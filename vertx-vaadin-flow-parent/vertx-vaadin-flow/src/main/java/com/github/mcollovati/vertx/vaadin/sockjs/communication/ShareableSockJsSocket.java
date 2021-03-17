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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.streams.Pipe;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * A wrapper for {@link SockJSSocket} just to implement {@link Shareable}
 * interface in order to let instance work with {@link io.vertx.core.shareddata.LocalMap}.
 */
class ShareableSockJsSocket implements SockJSSocket, Shareable {
    private final SockJSSocket socket;

    ShareableSockJsSocket(SockJSSocket socket) {
        this.socket = socket;
    }

    @Override
    public SockJSSocket exceptionHandler(Handler<Throwable> handler) {
        return socket.exceptionHandler(handler);
    }

    @Override
    public SockJSSocket handler(Handler<Buffer> handler) {
        return socket.handler(handler);
    }

    @Override
    public SockJSSocket pause() {
        return socket.pause();
    }

    @Override
    public SockJSSocket resume() {
        return socket.resume();
    }

    @Override
    public SockJSSocket endHandler(Handler<Void> endHandler) {
        return socket.endHandler(endHandler);
    }

    @Override
    public Future<Void> write(Buffer data) {
        return socket.write(data);
    }

    @Override
    public Future<Void> write(String data) {
        return socket.write(data);
    }

    @Override
    public void write(String data, Handler<AsyncResult<Void>> handler) {
        socket.write(data, handler);
    }

    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
        socket.write(data, handler);
    }

    @Override
    public SockJSSocket setWriteQueueMaxSize(int maxSize) {
        return socket.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public SockJSSocket drainHandler(Handler<Void> handler) {
        return socket.drainHandler(handler);
    }

    @Override
    public String writeHandlerID() {
        return socket.writeHandlerID();
    }

    @Override
    public Future<Void> end() {
        return socket.end();
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public void close(int statusCode, String reason) {
        socket.close(statusCode, reason);
    }

    @Override
    public SocketAddress remoteAddress() {
        return socket.remoteAddress();
    }

    @Override
    public SocketAddress localAddress() {
        return socket.localAddress();
    }

    @Override
    public MultiMap headers() {
        return socket.headers();
    }

    @Override
    public String uri() {
        return socket.uri();
    }

    @Override
    public RoutingContext routingContext() {
        return socket.routingContext();
    }

    @Override
    public Session webSession() {
        return socket.webSession();
    }

    @Override
    public User webUser() {
        return socket.webUser();
    }

    @Override
    public SockJSSocket fetch(long amount) {
        return socket.fetch(amount);
    }

    @Override
    public Pipe<Buffer> pipe() {
        return socket.pipe();
    }

    @Override
    public Future<Void> pipeTo(WriteStream<Buffer> dst) {
        return socket.pipeTo(dst);
    }

    @Override
    public void pipeTo(WriteStream<Buffer> dst, Handler<AsyncResult<Void>> handler) {
        socket.pipeTo(dst, handler);
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        socket.end(handler);
    }

    @Override
    public Future<Void> end(Buffer data) {
        return socket.end(data);
    }

    @Override
    public void end(Buffer data, Handler<AsyncResult<Void>> handler) {
        socket.end(data, handler);
    }

    @Override
    public boolean writeQueueFull() {
        return socket.writeQueueFull();
    }

}
