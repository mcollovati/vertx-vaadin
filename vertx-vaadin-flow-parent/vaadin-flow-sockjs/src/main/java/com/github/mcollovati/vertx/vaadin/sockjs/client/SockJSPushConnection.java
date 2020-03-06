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
package com.github.mcollovati.vertx.vaadin.sockjs.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.vaadin.client.Command;
import com.vaadin.client.Console;
import com.vaadin.client.Registry;
import com.vaadin.client.ResourceLoader;
import com.vaadin.client.ValueMap;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.communication.ConnectionStateHandler;
import com.vaadin.client.communication.MessageHandler;
import com.vaadin.client.communication.PushConfiguration;
import com.vaadin.client.communication.PushConnection;
import com.vaadin.client.communication.PushConnectionFactory;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.shared.util.SharedUtil;
import elemental.json.JsonObject;

public class SockJSPushConnection implements PushConnection {

    private final Registry registry;
    private SockJS socket;
    private SockJSConfiguration config;
    private State state = State.CONNECTING;
    private String transport;
    /**
     * Keeps track of the disconnect confirmation command for cases where
     * pending messages should be pushed before actually disconnecting.
     */
    private Command pendingDisconnectCommand;
    /**
     * The url to use for push requests
     */
    private String url;

    /**
     * Creates a new instance connected to the given registry.
     *
     * @param registry the global registry
     */
    public SockJSPushConnection(Registry registry) {
        this.registry = registry;
        registry.getUILifecycle().addHandler(event -> {
            if (event.getUiLifecycle().isTerminated()) {
                if (state == State.CLOSING
                    || state == State.CLOSED) {
                    return;
                }

                disconnect(() -> {
                });
            }
        });
        config = createConfig();
        // Always debug for now
        config.setStringValue("logLevel", "debug");

        getPushConfiguration().getParameters().forEach((value, key) -> {
            if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false")) {
                config.setBooleanValue(key, value.equalsIgnoreCase("true"));
            } else {
                config.setStringValue(key, value);
            }

        });
        config.mapTransports();
        if (getPushConfiguration().getPushUrl() != null) {
            url = getPushConfiguration().getPushUrl();
        } else {
            url = registry.getApplicationConfiguration().getServiceUrl();
        }
        runWhenSockJSLoaded(
            () -> Scheduler.get().scheduleDeferred(this::connect));
    }

    private PushConfiguration getPushConfiguration() {
        return registry.getPushConfiguration();
    }

    @Override
    public void push(JsonObject message) {
        if (!isBidirectional()) {
            throw new IllegalStateException(
                "This server to client push connection should not be used to send client to server messages");
        }
        if (state == State.OPEN) {
            String messageJson = WidgetUtil.stringify(message);
            getLogger().info("Sending push (" + transport
                + ") message to server: " + messageJson);

            doPush(socket, messageJson);
            return;
        }

        if (state == State.CONNECTING) {
            getConnectionStateHandler().pushNotConnected(message);
            return;
        }

        throw new IllegalStateException("Can not push after disconnecting");

    }

    private native void doPush(SockJS socket, String message)
    /*-{
       socket.send(message);
    }-*/;

    @Override
    public boolean isActive() {
        switch (state) {
            case CONNECTING:
            case OPEN:
                return true;
            default:
                return false;
        }
    }


    @Override
    public String getTransportType() {
        return transport;
    }

    @Override
    public boolean isBidirectional() {
        if (transport == null) {
            return false;
        }

        if (!"websocket".equals(transport)) {
            // If we are not using websockets, we want to send XHRs
            return false;
        }
        if (registry.getPushConfiguration().isAlwaysXhrToServer()) {
            // If user has forced us to use XHR, let's abide
            return false;
        }
        if (state == State.CONNECTING) {
            // Not sure yet, let's go for using websockets still as still will
            // delay the message until a connection is established. When the
            // connection is established, bi-directionality will be checked
            // again to be sure
        }
        return true;

    }

    private void connect() {
        String pushUrl = registry.getURIResolver().resolveVaadinUri(url);
        pushUrl = SharedUtil.addGetParameter(pushUrl,
            ApplicationConstants.REQUEST_TYPE_PARAMETER,
            ApplicationConstants.REQUEST_TYPE_PUSH);
        pushUrl = SharedUtil.addGetParameter(pushUrl,
            ApplicationConstants.UI_ID_PARAMETER,
            registry.getApplicationConfiguration().getUIId());

        String pushId = registry.getMessageHandler().getPushId();
        if (pushId != null) {
            pushUrl = SharedUtil.addGetParameter(pushUrl,
                ApplicationConstants.PUSH_ID_PARAMETER, pushId);
        }

        Console.log("Establishing push connection");
        socket = doConnect(pushUrl, getConfig());
    }

    @Override
    public void disconnect(Command command) {
        assert command != null;

        switch (state) {
            case CONNECTING:
                // Make the connection callback initiate the disconnection again
                state = State.CLOSING;
                pendingDisconnectCommand = command;
                break;
            case OPEN:
                // Normal disconnect
                Console.log("Closing push connection");
                doDisconnect(socket);
                state = State.CLOSED;
                command.execute();
                break;
            case CLOSING:
            case CLOSED:
                throw new IllegalStateException("Can not disconnect more than once");
            default:
                throw new IllegalStateException("Invalid state");
        }

    }

    protected SockJSConfiguration getConfig() {
        return config;
    }

    /*
     * Available options
     * - transport:
     * - websocket: options[maxLength, protocols]
     * - xhr-streaming: no-options
     * - xhr-polling: no-options
     * - transportOptions
     * - sessionId
     * - server
     */
    protected native SockJSConfiguration createConfig()
    /*-{
        return {
            transport: 'websocket',
            fallbackTransport: 'xhr-polling',
            transports: ['websocket', 'xhr-polling', 'xhr-streaming'],
            reconnectInterval: 5000,
            maxReconnectAttempts: 10
        };
    }-*/;

    protected void onReopen(JavaScriptObject response) {
        getLogger().info("Push connection re-established using " + socket.getTransport());
        onConnect(socket);
    }

    protected void onOpen(JavaScriptObject event) {
        getLogger().info(
            "Push connection established using " + socket.getTransport());
        onConnect(socket);
    }

    protected void onError(JavaScriptObject response) {
        state = State.CLOSED;
        getConnectionStateHandler().pushError(this, response);
    }

    protected void onClose(JavaScriptObject response) {
        state = State.CONNECTING;
        getConnectionStateHandler().pushClosed(this, response);
    }

    protected void onReconnect(JavaScriptObject response) {
        if (state == State.OPEN) {
            state = State.CONNECTING;
        }
        getConnectionStateHandler().pushReconnectPending(this);
    }

    protected void onMessage(TransportMessageEvent response) {
        String message = response.getResponseBody();
        ValueMap json = MessageHandler.parseWrappedJson(message);
        if (json == null) {
            // Invalid string (not wrapped as expected)
            getConnectionStateHandler().pushInvalidContent(this, message);
        } else {
            Console.log("Received push (" + getTransportType() + ") message: " + message);
            registry.getMessageHandler().handleMessage(json);
        }
    }


    protected void onConnect(SockJS socket) {
        transport = socket.getTransport();
        switch (state) {
            case CONNECTING:
                state = State.OPEN;
                getConnectionStateHandler().pushOk(this);
                break;
            case CLOSING:
                // Set state to connected to make disconnect close the connection
                state = State.OPEN;
                assert pendingDisconnectCommand != null;
                disconnect(pendingDisconnectCommand);
                break;
            case OPEN:
                // IE likes to open the same connection multiple times, just ignore
                break;
            default:
                throw new IllegalStateException(
                    "Got onOpen event when conncetion state is " + state
                        + ". This should never happen.");
        }
    }

    private native SockJS doConnect(String uri,
                                    JavaScriptObject config)
    /*-{
        var self = this;


        config.url = uri;
        config.onOpen = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onOpen(*)(response);
        });
        config.onRepen = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onReopen(*)(response);
        });
        config.onMessage = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onMessage(*)(response);
        });
        config.onError = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onError(*)(response);
        });
        config.onClose = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onClose(*)(response);
        });
        config.onReconnect = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.sockjs.client.SockJSPushConnection::onReconnect(*)(response);
        });


        return $wnd.vaadinPush.SockJS.connect(config);
    }-*/;

    private void runWhenSockJSLoaded(final Command command) {
        if (isSockJSLoaded()) {
            command.execute();
        } else {
            final String pushJs = getVersionedPushJs();

            Console.log("Loading " + pushJs);
            ResourceLoader loader = registry.getResourceLoader();
            String pushScriptUrl = registry.getApplicationConfiguration()
                .getContextRootUrl() + pushJs;

            ResourceLoader.ResourceLoadListener loadListener = new ResourceLoader.ResourceLoadListener() {
                @Override
                public void onLoad(ResourceLoader.ResourceLoadEvent event) {
                    if (isSockJSLoaded()) {
                        Console.log(pushJs + " loaded");
                        command.execute();
                    } else {
                        // If bootstrap tried to load vaadinPush.js,
                        // ResourceLoader assumes it succeeded even if
                        // it failed (#11673)
                        onError(event);
                    }
                }

                @Override
                public void onError(ResourceLoader.ResourceLoadEvent event) {
                    getConnectionStateHandler().pushScriptLoadError(
                        event.getResourceUrl());
                }
            };
            loader.loadScript(pushScriptUrl, loadListener);
        }
    }

    private ConnectionStateHandler getConnectionStateHandler() {
        return registry.getConnectionStateHandler();
    }

    private String getVersionedPushJs() {
        String pushJs;
        if (registry.getApplicationConfiguration().isProductionMode()) {
            pushJs = "vaadinPushSockJS-min.js";
        } else {
            pushJs = "vaadinPushSockJS.js";
        }
        return ApplicationConstants.VAADIN_STATIC_FILES_PATH + "push/" + pushJs;
    }

    private static native boolean isSockJSLoaded()
    /*-{
        return $wnd.vaadinPush && $wnd.vaadinPush.SockJS;
    }-*/;

    private static native void doDisconnect(SockJS sock)
    /*-{
       sock.close();
    }-*/;


    private static Logger getLogger() {
        return Logger.getLogger(SockJSPushConnection.class.getName());
    }

    private enum State {
        CONNECTING, OPEN, CLOSING, CLOSED
    }

    public abstract static class AbstractJSO extends JavaScriptObject {
        protected AbstractJSO() {
        }

        protected final native String getStringValue(String key)
        /*-{
           return this[key];
         }-*/;

        protected final native void setStringValue(String key, String value)
        /*-{
            this[key] = value;
        }-*/;

        protected final native int getIntValue(String key)
        /*-{
           return this[key];
         }-*/;

        protected final native void setIntValue(String key, int value)
        /*-{
            this[key] = value;
        }-*/;

        protected final native boolean getBooleanValue(String key)
        /*-{
           return this[key];
         }-*/;

        protected final native void setBooleanValue(String key, boolean value)
        /*-{
            this[key] = value;
        }-*/;

    }

    public static class SockJSConfiguration extends AbstractJSO {

        // Vaadin to SockJS transport map
        private static final Map<String, String> TRANSPORT_MAPPER = new HashMap<>();

        static {
            TRANSPORT_MAPPER.put("websocket", "websocket");
            TRANSPORT_MAPPER.put("websocket-xhr", "websocket");
            TRANSPORT_MAPPER.put("long-polling", "xhr-polling");
            TRANSPORT_MAPPER.put("streaming", "xhr-streaming");
        }

        protected SockJSConfiguration() {
        }

        public final String getTransport() {
            return getStringValue("transport");
        }

        public final void setTransport(String transport) {
            setStringValue("transport", TRANSPORT_MAPPER.getOrDefault(transport, transport));
        }

        public final String getFallbackTransport() {
            return getStringValue("fallbackTransport");
        }

        public final void setFallbackTransport(String fallbackTransport) {
            setStringValue("fallbackTransport", TRANSPORT_MAPPER.getOrDefault(fallbackTransport, fallbackTransport));
        }

        public final void mapTransports() {
            setTransport(getTransport());
            setFallbackTransport(getFallbackTransport());
            setTransports();
        }

        private native void setTransports()
        /*-{
            this.transports = [this.transport, this.fallbackTransport];
        }-*/;


    }

    public static class TransportMessageEvent extends AbstractJSO {

        protected TransportMessageEvent() {
        }

        protected final String getResponseBody() {
            return getStringValue("data");
        }

    }

    public static class SockJS extends AbstractJSO {

        protected SockJS() {
        }

        protected native final String getTransport()
        /*-{
            return this.getTransport();
        }-*/;

        protected native final int readyState()
        /*-{
            return this.getReadyState();
        }-*/;

    }

    static class Factory implements PushConnectionFactory {

        @Override
        public PushConnection create(Registry registry) {
            return new SockJSPushConnection(registry);
        }
    }
}
