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
package com.github.mcollovati.vertx.vaadin.sockjs.communication;

import java.io.Reader;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.PushConnection;
import com.vaadin.flow.server.communication.UidlWriter;
import elemental.json.JsonObject;

public class SockJSPushConnection implements PushConnection {

    private final int uiId;
    private PushSocket socket;
    private State state = State.DISCONNECTED;
    private transient Future<?> outgoingMessage;

    public SockJSPushConnection(UI ui) {
        this.uiId = ui.getUIId();
    }

    @Override
    public void push() {
        push(true);
    }

    /**
     * Pushes pending state changes and client RPC calls to the client. If
     * {@code isConnected()} is false, defers the push until a connection is
     * established.
     *
     * @param async True if this push asynchronously originates from the server,
     *              false if it is a response to a client request.
     */
    void push(boolean async) {
        if (!isConnected()) {
            if (async && state != State.RESPONSE_PENDING) {
                state = State.PUSH_PENDING;
            } else {
                state = State.RESPONSE_PENDING;
            }
        } else {
            try {
                UI ui = VaadinSession.getCurrent().getUIById(this.uiId);
                JsonObject response = new UidlWriter().createUidl(ui, async);
                sendMessage("for(;;);[" + response.toJson() + "]");
            } catch (Exception e) {
                throw new PushException("Push failed", e);
            }
        }
    }

    private void sendMessage(String message) {
        this.outgoingMessage = socket.send(message).toCompletableFuture();
    }

    protected Reader receiveMessage(Reader data) {
        // SockJS will always receive the whole message
        return data;
    }


    @Override
    public void disconnect() {
        assert isConnected();
        if (socket == null) {
            // Already disconnected. Should not happen but if it does, we don't
            // want to cause NPEs
            getLogger().fine(
                "SockJSPushConnection.disconnect() called twice, this should not happen");
            return;
        }

        if (outgoingMessage != null) {
            // Wait for the last message to be sent before closing the
            // connection (assumes that futures are completed in order)
            try {
                outgoingMessage.get(1000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                getLogger().log(Level.INFO,
                    "Timeout waiting for messages to be sent to client before disconnect");
            } catch (Exception e) {
                getLogger().log(Level.INFO,
                    "Error waiting for messages to be sent to client before disconnect");
            }
            outgoingMessage = null;
        }

        // Should block until disconnection happens
        try {
            this.socket.close().thenRun(this::connectionLost)
                .toCompletableFuture().get();
        } catch (Exception e) {
            getLogger().log(Level.INFO, "Error waiting for disconnection");
            this.connectionLost();
        }
    }

    @Override
    public boolean isConnected() {
        return state == State.CONNECTED && socket != null && socket.isConnected();
    }

    void connect(PushSocket socket) {
        assert socket != null;
        assert socket != this.socket;

        if (isConnected()) {
            disconnect();
        }

        this.socket = socket;
        State oldState = state;
        state = State.CONNECTED;

        if (oldState == State.PUSH_PENDING
            || oldState == State.RESPONSE_PENDING) {
            // Sending a "response" message (async=false) also takes care of a
            // pending push, but not vice versa
            push(oldState == State.PUSH_PENDING);
        }

    }

    void connectionLost() {
        socket = null;
        if (state == State.CONNECTED) {
            // Guard against connectionLost being (incorrectly) called when
            // state is PUSH_PENDING or RESPONSE_PENDING
            // (http://dev.vaadin.com/ticket/16919)
            state = State.DISCONNECTED;
        }
    }

    PushSocket getSocket() {
        return socket;
    }

    private static Logger getLogger() {
        return Logger.getLogger(SockJSPushConnection.class.getName());
    }

    protected enum State {
        /**
         * Not connected. Trying to push will set the connection state to
         * PUSH_PENDING or RESPONSE_PENDING and defer sending the message until
         * a connection is established.
         */
        DISCONNECTED,

        /**
         * Not connected. An asynchronous push is pending the opening of the
         * connection.
         */
        PUSH_PENDING,

        /**
         * Not connected. A response to a client request is pending the opening
         * of the connection.
         */
        RESPONSE_PENDING,

        /**
         * Connected. Messages can be sent through the connection.
         */
        CONNECTED;

    }

}
