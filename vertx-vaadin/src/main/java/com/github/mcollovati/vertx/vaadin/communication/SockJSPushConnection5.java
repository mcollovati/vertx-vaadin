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
package com.github.mcollovati.vertx.vaadin.communication;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.communication.PushConnection;
import com.vaadin.server.communication.UidlWriter;
import com.vaadin.ui.UI;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

public class SockJSPushConnection5 implements PushConnection {


    private final UI ui;
    private transient SockJSSocket socket;
    private transient State state = State.DISCONNECTED;
    private transient Future<Object> outgoingMessage;

    public SockJSPushConnection5(UI ui) {
        this.ui = ui;
    }

    @Override
    public void push() {
        push(true);
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

        /* TOO: verify
        if (resource.isResumed()) {
            // This can happen for long polling because of
            // http://dev.vaadin.com/ticket/16919
            // Once that is fixed, this should never happen
            connectionLost();
            return;                                     ]}]
        }
        */

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

        try {
            socket.close();
        } catch (Exception e) {
            getLogger().log(Level.INFO, "Error when closing push connection",
                e);
        }
        connectionLost();

    }

    public void connectionLost() {
        socket = null;
        if (state == State.CONNECTED) {
            // Guard against connectionLost being (incorrectly) called when
            // state is PUSH_PENDING or RESPONSE_PENDING
            // (http://dev.vaadin.com/ticket/16919)
            state = State.DISCONNECTED;
        }
    }


    @Override
    public boolean isConnected() {
        assert state != null;
        assert (state == State.CONNECTED) ^ (socket == null);
        return state == State.CONNECTED;
    }

    void connect(SockJSSocket socket) {

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
                Writer writer = new StringWriter();
                new UidlWriter().write(getUI(), writer, async);
                sendMessage("for(;;);[{" + writer + "}]");
            } catch (Exception e) {
                throw new RuntimeException("Push failed", e);
            }
        }
    }

    /**
     * Sends the given message to the current client. Cannot be called if
     * {@link SockJSPushConnection#isConnected()} is false.
     *
     * @param message The message to send
     */
    protected void sendMessage(String message) {
        assert (isConnected());
        // "Broadcast" the changes to the single client only
        outgoingMessage = CompletableFuture.supplyAsync(() -> {
            socket.write(Buffer.buffer(message, "UTF-8"));
            return message;
        });
    }


    protected Reader receiveMessage(Buffer data) {
        // SockJS will always receive the whole message
        return new StringReader(data.toString());
    }


    UI getUI() {
        return ui;
    }

    SockJSSocket getSocket() {
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
