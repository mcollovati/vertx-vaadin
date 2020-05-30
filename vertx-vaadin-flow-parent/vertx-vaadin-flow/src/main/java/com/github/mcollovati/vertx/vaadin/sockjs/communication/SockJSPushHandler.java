/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.vaadin.sockjs.communication;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.github.mcollovati.vertx.http.HttpServerResponseWrapper;
import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.ServletHelper;
import com.vaadin.flow.server.SessionExpiredException;
import com.vaadin.flow.server.SystemMessages;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.PushConnection;
import com.vaadin.flow.server.communication.ServerRpcHandler;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.shared.communication.PushMode;
import elemental.json.JsonException;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.impl.RoutingContextDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles incoming push connections and messages and dispatches them to the
 * correct {@link UI}/ {@link SockJSPushConnection}.
 * <p>
 * Source code adapted from Vaadin {@link com.vaadin.flow.server.communication.PushHandler}
 */
public class SockJSPushHandler implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(SockJSPushHandler.class);

    /**
     * Callback used when we receive a UIDL request through Atmosphere. If the
     * push channel is bidirectional (websockets), the request was sent via the
     * same channel. Otherwise, the client used a separate AJAX request. Handle
     * the request and send changed UI state via the push channel (we do not
     * respond to the request directly.)
     */
    private final PushEventCallback receiveCallback = (PushEvent event, UI ui) -> {

        logger.debug("Received message from resource {}", event.socket().getUUID());

        PushSocket socket = event.socket;
        SockJSPushConnection connection = getConnectionForUI(ui);

        assert connection != null : "Got push from the client "
            + "even though the connection does not seem to be "
            + "valid. This might happen if a HttpSession is "
            + "serialized and deserialized while the push "
            + "connection is kept open or if the UI has a "
            + "connection of unexpected type.";


        Reader reader = event.message()
            .map(data -> new StringReader(data.toString()))
            .map(connection::receiveMessage).orElse(null);
        if (reader == null) {
            // The whole message was not yet received
            return;
        }


        // Should be set up by caller
        VaadinRequest vaadinRequest = VaadinService.getCurrentRequest();
        assert vaadinRequest != null;

        try {
            new ServerRpcHandler().handleRpc(ui, reader, vaadinRequest);
            connection.push(false);
        } catch (JsonException e) {
            logger.error("Error writing JSON to response", e);
            // Refresh on client side
            sendRefreshAndDisconnect(socket);
        } catch (ServerRpcHandler.InvalidUIDLSecurityKeyException e) {
            logger.warn("Invalid security key received from {}", socket.remoteAddress());
            // Refresh on client side
            sendRefreshAndDisconnect(socket);
        }
    };
    private final VertxVaadinService service;
    private final Router router;
    private final SessionHandler sessionHandler;
    private final LocalMap<String, SockJSSocket> connectedSocketsLocalMap;

    /**
     * Callback used when we receive a request to establish a push channel for a
     * UI. Associate the SockJS socket with the UI and leave the connection
     * open. If there is a pending push, send it now.
     */
    private final PushEventCallback establishCallback = (PushEvent event, UI ui) -> {
        logger.trace("New push connection for resource {} with transport {}", event.socket().getUUID(), "resource.transport()");

        VaadinSession session = ui.getSession();
        PushSocket socket = event.socket;

        HttpServerRequest request = event.routingContext.request();

        String requestToken = request.getParam(ApplicationConstants.PUSH_ID_PARAMETER);
        if (!isPushIdValid(session, requestToken)) {
            logger.warn("Invalid identifier in new connection received from {}", socket.remoteAddress());
            // Refresh on client side, create connection just for
            // sending a message
            sendRefreshAndDisconnect(socket);
            return;
        }

        SockJSPushConnection connection = getConnectionForUI(ui);
        assert (connection != null);

        connection.connect(socket);
    };

    public SockJSPushHandler(VertxVaadinService service, SessionHandler sessionHandler, SockJSHandler sockJSHandler) {
        this.service = service;
        this.sessionHandler = sessionHandler;
        this.connectedSocketsLocalMap = socketsMap(service.getVertx());
        this.router = sockJSHandler.socketHandler(this::onConnect);
    }

    private void onConnect(SockJSSocket sockJSSocket) {
        RoutingContext routingContext = CurrentInstance.get(RoutingContext.class);

        String uuid = sockJSSocket.writeHandlerID();
        // Send an ACK
        sockJSSocket.write("ACK-CONN|" + uuid);

        connectedSocketsLocalMap.put(uuid, sockJSSocket);
        PushSocket socket = new PushSocketImpl(sockJSSocket);

        initSocket(sockJSSocket, routingContext, socket);

        // Send an ACK
        //socket.send("ACK-CONN|" + uuid);

        sessionHandler.handle(new SockJSRoutingContext(routingContext, rc ->
            callWithUi(new PushEvent(socket, routingContext, null), establishCallback)
        ));
    }

    private void initSocket(SockJSSocket sockJSSocket, RoutingContext routingContext, PushSocket socket) {
        sockJSSocket.handler(data -> sessionHandler.handle(
            new SockJSRoutingContext(routingContext, rc -> onMessage(new PushEvent(socket, rc, data)))
        ));
        sockJSSocket.endHandler(unused -> sessionHandler.handle(
            new SockJSRoutingContext(routingContext, rc -> onDisconnect(new PushEvent(socket, rc, null)))
        ));
        sockJSSocket.exceptionHandler(t -> sessionHandler.handle(
            new SockJSRoutingContext(routingContext, rc -> onError(new PushEvent(socket, routingContext, null), t))
        ));

    }

    private void onDisconnect(PushEvent ev) {
        connectedSocketsLocalMap.remove(ev.socket.getUUID());
        connectionLost(ev);
    }

    private void onError(PushEvent ev, Throwable t) {
        logger.error("Exception in push connection", t);
        connectionLost(ev);
    }

    private void onMessage(PushEvent event) {
        callWithUi(event, receiveCallback);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        CurrentInstance.set(RoutingContext.class, routingContext);
        try {
            router.handleContext(routingContext);
        } finally {
            CurrentInstance.set(RoutingContext.class, null);
        }
    }

    private void callWithUi(final PushEvent event, final PushEventCallback callback) {

        PushSocket socket = event.socket;
        RoutingContext routingContext = event.routingContext;
        VertxVaadinRequest vaadinRequest = new VertxVaadinRequest(service, routingContext);
        VaadinSession session = null;

        service.requestStart(vaadinRequest, null);
        try {
            try {
                session = service.findVaadinSession(vaadinRequest);
                assert VaadinSession.getCurrent() == session;
            } catch (SessionExpiredException e) {
                sendNotificationAndDisconnect(socket, VaadinService.createSessionExpiredJSON());
                return;
            }

            UI ui = null;
            session.lock();
            try {
                ui = service.findUI(vaadinRequest);
                assert UI.getCurrent() == ui;

                if (ui == null) {
                    sendNotificationAndDisconnect(
                        socket, VaadinService.createUINotFoundJSON()
                    );
                } else {
                    callback.run(event, ui);
                }
            } catch (final IOException e) {
                callErrorHandler(session, e);
            } catch (final Exception e) {
                SystemMessages msg = service.getSystemMessages(
                    ServletHelper.findLocale(null, vaadinRequest),
                    vaadinRequest);

                /* TODO: verify */
                PushSocket errorSocket = getOpenedPushConnection(socket, ui);
                sendNotificationAndDisconnect(errorSocket,
                    VaadinService.createCriticalNotificationJSON(
                        msg.getInternalErrorCaption(),
                        msg.getInternalErrorMessage(), null,
                        msg.getInternalErrorURL()));
                callErrorHandler(session, e);
            } finally {
                try {
                    session.unlock();
                } catch (Exception e) {
                    logger.warn("Error while unlocking session", e);
                    // can't call ErrorHandler, we (hopefully) don't have a lock
                }
            }
        } finally {
            try {
                service.requestEnd(vaadinRequest, null, session);
            } catch (Exception e) {
                logger.warn("Error while ending request", e);

                // can't call ErrorHandler, we don't have a lock
            }

        }
    }

    private PushSocket getOpenedPushConnection(PushSocket socket, UI ui) {
        PushSocket errorSocket = socket;
        if (ui != null && ui.getInternals().getPushConnection() != null) {
            // We MUST use the opened push connection if there is one.
            // Otherwise we will write the response to the wrong request
            // when using streaming (the client -> server request
            // instead of the opened push channel)
            errorSocket = ((SockJSPushConnection) ui.getInternals().getPushConnection()).getSocket();
        }
        return errorSocket;
    }

    /**
     * Call the session's {@link ErrorHandler}, if it has one, with the given
     * exception wrapped in an {@link ErrorEvent}.
     */
    private void callErrorHandler(VaadinSession session, Exception e) {
        try {
            ErrorHandler errorHandler = ErrorEvent.findErrorHandler(session);
            if (errorHandler != null) {
                errorHandler.error(new ErrorEvent(e));
            }
        } catch (Exception ex) {
            // Let's not allow error handling to cause trouble; log fails
            logger.warn("ErrorHandler call failed", ex);
        }
    }

    void connectionLost(PushEvent event) {
        // We don't want to use callWithUi here, as it assumes there's a client
        // request active and does requestStart and requestEnd among other
        // things.

        VaadinRequest vaadinRequest = new VertxVaadinRequest(service, event.routingContext);
        VaadinSession session;

        try {
            session = service.findVaadinSession(vaadinRequest);
        } catch (SessionExpiredException e) {
            // This happens at least if the server is restarted without
            // preserving the session. After restart the client reconnects, gets
            // a session expired notification and then closes the connection and
            // ends up here
            logger.trace("Session expired before push disconnect event was received", e);
            return;
        }

        UI ui;
        session.lock();
        try {
            VaadinSession.setCurrent(session);
            // Sets UI.currentInstance
            ui = service.findUI(vaadinRequest);
            if (ui == null) {
                /*
                 * UI not found, could be because FF has asynchronously closed
                 * the websocket connection and Atmosphere has already done
                 * cleanup of the request attributes.
                 *
                 * In that case, we still have a chance of finding the right UI
                 * by iterating through the UIs in the session looking for one
                 * using the same AtmosphereResource.
                 */
                ui = findUiUsingSocket(event.socket(), session.getUIs());

                if (ui == null) {
                    logger.debug(
                        "Could not get UI. This should never happen,"
                            + " except when reloading in Firefox and Chrome -"
                            + " see http://dev.vaadin.com/ticket/14251.");
                    return;
                } else {
                    logger.info(
                        "No UI was found based on data in the request,"
                            + " but a slower lookup based on the AtmosphereResource succeeded."
                            + " See http://dev.vaadin.com/ticket/14251 for more details.");
                }
            }

            PushMode pushMode = ui.getPushConfiguration().getPushMode();
            SockJSPushConnection pushConnection = getConnectionForUI(ui);

            String id = event.socket().getUUID();

            if (pushConnection == null) {
                logger.warn("Could not find push connection to close: {} with transport {}", id, "resource.transport()");
            } else {
                if (!pushMode.isEnabled()) {
                    /*
                     * The client is expected to close the connection after push
                     * mode has been set to disabled.
                     */
                    logger.trace("Connection closed for resource {}", id);
                } else {
                    /*
                     * Unexpected cancel, e.g. if the user closes the browser
                     * tab.
                     */
                    logger.trace("Connection unexpectedly closed for resource {} with transport {}", id, "resource.transport()");
                }

                pushConnection.connectionLost();
            }

        } catch (final Exception e) {
            callErrorHandler(session, e);
        } finally {
            try {
                session.unlock();
            } catch (Exception e) {
                logger.warn("Error while unlocking session", e);
                // can't call ErrorHandler, we (hopefully) don't have a lock
            }
        }
    }

    private static LocalMap<String, SockJSSocket> socketsMap(Vertx vertx) {
        return vertx.sharedData().getLocalMap(SockJSPushHandler.class.getName() + ".push-sockets");
    }

    /**
     * Checks whether a given push id matches the session's push id.
     *
     * @param session       the vaadin session for which the check should be done
     * @param requestPushId the push id provided in the request
     * @return {@code true} if the id is valid, {@code false} otherwise
     */
    private static boolean isPushIdValid(VaadinSession session, String requestPushId) {

        String sessionPushId = session.getPushId();
        return requestPushId != null && requestPushId.equals(sessionPushId);
    }

    /**
     * Tries to send a critical notification to the client and close the
     * connection. Does nothing if the connection is already closed.
     */
    private static void sendNotificationAndDisconnect(
        PushSocket socket, String notificationJson) {
        try {
            socket.send(notificationJson);
            socket.close();
        } catch (Exception e) {
            logger.trace("Failed to send critical notification to client", e);
        }
    }

    private static SockJSPushConnection getConnectionForUI(UI ui) {
        PushConnection pushConnection = ui.getInternals().getPushConnection();
        if (pushConnection instanceof SockJSPushConnection) {
            return (SockJSPushConnection) pushConnection;
        } else {
            return null;
        }
    }

    private static UI findUiUsingSocket(PushSocket socket, Collection<UI> uIs) {
        for (UI ui : uIs) {
            PushConnection pushConnection = ui.getInternals().getPushConnection();
            if (pushConnection instanceof SockJSPushConnection &&
                ((SockJSPushConnection) pushConnection).getSocket() == socket) {
                return ui;
            }
        }
        return null;
    }

    private static void sendRefreshAndDisconnect(PushSocket socket) {
        sendNotificationAndDisconnect(socket, VaadinService
            .createCriticalNotificationJSON(null, null, null, null));
    }

    private interface PushEventCallback {
        void run(PushEvent event, UI ui) throws IOException;
    }

    private static class PushSocketImpl implements PushSocket {

        private final String socketUUID;
        private final String remoteAddress;

        PushSocketImpl(SockJSSocket socket) {
            this.socketUUID = socket.writeHandlerID();
            this.remoteAddress = socket.remoteAddress().toString();
        }

        @Override
        public String getUUID() {
            return socketUUID;
        }

        @Override
        public String remoteAddress() {
            return remoteAddress;
        }

        @Override
        public CompletionStage<?> send(String message) {
            return runCommand(socket -> {
                socket.write(Buffer.buffer(message));
                return Boolean.TRUE;
            });
        }

        @Override
        public CompletionStage<Boolean> close() {
            return runCommand(socket -> {
                socket.close();
                return Boolean.TRUE;
            });
        }

        @Override
        public boolean isConnected() {
            return tryGetSocket() != null;
        }

        private SockJSSocket tryGetSocket() {
            Vertx vertx = Vertx.currentContext().owner();
            return SockJSPushHandler.socketsMap(vertx).get(socketUUID);
        }

        private <T> CompletableFuture<T> runCommand(Function<SockJSSocket, T> action) {
            CompletableFuture<T> future = new CompletableFuture<>();
            SockJSSocket socket = tryGetSocket();
            if (socket != null) {
                future = CompletableFuture.supplyAsync(() -> action.apply(socket));
            } else {
                future.completeExceptionally(new RuntimeException("Socket not registered: " + socketUUID));
            }
            return future;
        }
    }

    private static class PushEvent {
        private final Buffer message;
        private final RoutingContext routingContext;
        private final PushSocket socket;

        PushEvent(PushSocket socket, RoutingContext routingContext, Buffer message) {
            this.message = message;
            this.routingContext = routingContext;
            this.socket = socket;
        }

        PushSocket socket() {
            return socket;
        }

        Optional<Buffer> message() {
            return Optional.ofNullable(message);
        }
    }


}

class SockJSRoutingContext extends RoutingContextDecorator {

    private final List<Handler<Void>> headersEndHandlers = new ArrayList<>();
    private final Handler<RoutingContext> action;
    private Session session;

    SockJSRoutingContext(RoutingContext source, Handler<RoutingContext> action) {
        super(source.currentRoute(), source);
        this.action = action;
    }


    @Override
    public HttpServerResponse response() {
        return new HttpServerResponseWrapper(super.response()) {
            @Override
            public int getStatusCode() {
                return 200;
            }
        };

    }

    @Override
    public Session session() {
        return this.session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void next() {
        vertx().runOnContext(future -> {
            action.handle(this);
            headersEndHandlers.forEach(h -> h.handle(null));
        });
    }

    @Override
    public int addHeadersEndHandler(Handler<Void> handler) {
        headersEndHandlers.add(handler);
        return headersEndHandlers.size();
    }

}