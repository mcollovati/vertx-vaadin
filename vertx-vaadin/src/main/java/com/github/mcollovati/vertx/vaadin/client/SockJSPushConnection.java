/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ResourceLoader;
import com.vaadin.client.ValueMap;
import com.vaadin.client.communication.ConnectionStateHandler;
import com.vaadin.client.communication.MessageHandler;
import com.vaadin.client.communication.PushConnection;
import com.vaadin.shared.ApplicationConstants;
import com.vaadin.shared.Version;
import com.vaadin.shared.ui.ui.UIConstants;
import com.vaadin.shared.ui.ui.UIState;
import com.vaadin.shared.util.SharedUtil;
import elemental.json.JsonObject;

public class SockJSPushConnection implements PushConnection {

    private ApplicationConnection connection;
    private SockJS socket;
    private SockJSConfiguration config;
    private State state = State.CONNECTING;
    private String uri;
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

    @Override
    public void init(ApplicationConnection connection, UIState.PushConfigurationState pushConfiguration) {
        this.connection = connection;

        connection.addHandler(ApplicationConnection.ApplicationStoppedEvent.TYPE,
            event -> {
                if (state == State.CLOSING
                    || state == State.CLOSED) {
                    return;
                }

                disconnect(() -> {
                });
            });

        config = createConfig();
        String debugParameter = Window.Location.getParameter("debug");
        if ("push".equals(debugParameter)) {
            config.setStringValue("logLevel", "debug");
        }
        for (String param : pushConfiguration.parameters.keySet()) {
            String value = pushConfiguration.parameters.get(param);
            if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false")) {
                config.setBooleanValue(param, value.equalsIgnoreCase("true"));
            } else {
                config.setStringValue(param, value);
            }
        }
        config.mapTransports();
        if (pushConfiguration.pushUrl != null) {
            url = pushConfiguration.pushUrl;
        } else {
            url = ApplicationConstants.APP_PROTOCOL_PREFIX
                + ApplicationConstants.PUSH_PATH;
        }
        runWhenSockJSLoaded(
            () -> Scheduler.get().scheduleDeferred(this::connect));

    }


    @Override
    public void push(JsonObject message) {
        if (!isBidirectional()) {
            throw new IllegalStateException(
                "This server to client push connection should not be used to send client to server messages");
        }
        if (state == State.OPEN) {
            getLogger().info("Sending push (" + transport
                + ") message to server: " + message.toJson());

            doPush(socket, message.toJson());
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

        if (!transport.equals("websocket")) {
            // If we are not using websockets, we want to send XHRs
            return false;
        }
        if (getPushConfigurationState().alwaysUseXhrForServerRequests) {
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
        String baseUrl = connection.translateVaadinUri(url);
        String extraParams = UIConstants.UI_ID_PARAMETER + "="
            + connection.getConfiguration().getUIId();

        String pushId = connection.getMessageHandler().getPushId();
        if (pushId != null) {
            extraParams += "&" + ApplicationConstants.PUSH_ID_PARAMETER + "="
                + pushId;
        }

        // uri is needed to identify the right connection when closing
        uri = SharedUtil.addGetParameters(baseUrl, extraParams);

        getLogger().info("Establishing push connection");
        socket = doConnect(uri, getConfig());
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
                getLogger().info("Closing push connection");
                doDisconnect(socket);
                state = State.CLOSED;
                command.execute();
                break;
            case CLOSING:
            case CLOSED:
                throw new IllegalStateException(
                    "Can not disconnect more than once");
        }

    }

    protected SockJSConfiguration getConfig() {
        return config;
    }

    /**
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
            getLogger().info("Received push (" + getTransportType()
                + ") message: " + message);
            connection.getMessageHandler().handleMessage(json);
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
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onOpen(*)(response);
        });
        config.onRepen = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onReopen(*)(response);
        });
        config.onMessage = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onMessage(*)(response);
        });
        config.onError = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onError(*)(response);
        });
        config.onClose = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onClose(*)(response);
        });
        config.onReconnect = $entry(function(response) {
            self.@com.github.mcollovati.vertx.vaadin.client.SockJSPushConnection::onReconnect(*)(response);
        });


        return $wnd.vaadinPush.SockJS.connect(config);
    }-*/;

    private void runWhenSockJSLoaded(final Command command) {
        if (isSockJSLoaded()) {
            command.execute();
        } else {
            final String pushJs = getVersionedPushJs();

            getLogger().info("Loading " + pushJs);
            ResourceLoader.get().loadScript(
                connection.getConfiguration().getVaadinDirUrl() + pushJs,
                new ResourceLoader.ResourceLoadListener() {
                    @Override
                    public void onLoad(ResourceLoader.ResourceLoadEvent event) {
                        if (isSockJSLoaded()) {
                            getLogger().info(pushJs + " loaded");
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
                });
        }
    }

    private UIState.PushConfigurationState getPushConfigurationState() {
        return connection.getUIConnector().getState().pushConfiguration;
    }

    private ConnectionStateHandler getConnectionStateHandler() {
        return connection.getConnectionStateHandler();
    }

    private String getVersionedPushJs() {
        String pushJs;
        if (ApplicationConfiguration.isProductionMode()) {
            pushJs = "vaadinPushSockJS.js";
        } else {
            pushJs = "vaadinPushSockJS.js";
        }
        // Parameter appended to bypass caches after version upgrade.
        pushJs += "?v=" + Version.getFullVersion();
        return pushJs;
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

}
