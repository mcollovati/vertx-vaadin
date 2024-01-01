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
package com.github.mcollovati.vertx.web.sstore;

import java.util.Objects;
import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.impl.LocalSessionStoreImpl;

class ExtendedLocalSessionStoreImpl implements ExtendedLocalSessionStore {

    private final LocalMap<String, Session> localMap;
    private final LocalSessionStore sessionsStore;
    private Handler<AsyncResult<String>> expirationHandler = x -> {};

    public ExtendedLocalSessionStoreImpl(Vertx vertx, String sessionMapName, long reaperInterval) {
        this.localMap = vertx.sharedData().getLocalMap(sessionMapName);
        this.sessionsStore = new LocalSessionStoreImpl() {
            @Override
            public synchronized void handle(Long tid) {
                notifyExpiredSessions(() -> super.handle(tid));
            }
        };
        this.sessionsStore.init(
                vertx, new JsonObject().put("mapName", sessionMapName).put("reaperInterval", reaperInterval));
    }

    @Override
    public ExtendedSessionStore init(Vertx vertx, JsonObject options) {
        return this;
    }

    @Override
    public ExtendedLocalSessionStore expirationHandler(Handler<AsyncResult<String>> handler) {
        this.expirationHandler = Objects.requireNonNull(handler);
        return this;
    }

    @Override
    public long retryTimeout() {
        return sessionsStore.retryTimeout();
    }

    @Override
    public Session createSession(long timeout) {
        return sessionsStore.createSession(timeout);
    }

    @Override
    public Session createSession(long timeout, int length) {
        return sessionsStore.createSession(timeout, length);
    }

    @Override
    public ExtendedSessionStore get(String id, Handler<AsyncResult<Session>> resultHandler) {
        sessionsStore.get(id, resultHandler);
        return this;
    }

    @Override
    public Future<Session> get(String cookieValue) {
        return sessionsStore.get(cookieValue);
    }

    @Override
    public ExtendedSessionStore delete(String id, Handler<AsyncResult<Void>> resultHandler) {
        sessionsStore.delete(id, resultHandler);
        return this;
    }

    @Override
    public Future<Void> delete(String cookieValue) {
        return sessionsStore.delete(cookieValue);
    }

    @Override
    public ExtendedSessionStore put(Session session, Handler<AsyncResult<Void>> resultHandler) {
        sessionsStore.put(session, resultHandler);
        return this;
    }

    @Override
    public Future<Void> put(Session session) {
        return sessionsStore.put(session);
    }

    @Override
    public ExtendedSessionStore clear(Handler<AsyncResult<Void>> resultHandler) {
        sessionsStore.clear(resultHandler);
        return this;
    }

    @Override
    public Future<Void> clear() {
        return sessionsStore.clear();
    }

    @Override
    public ExtendedSessionStore size(Handler<AsyncResult<Integer>> resultHandler) {
        sessionsStore.size(resultHandler);
        return this;
    }

    @Override
    public Future<Integer> size() {
        return sessionsStore.size();
    }

    @Override
    public void close() {
        sessionsStore.close();
    }

    private void notifyExpiredSessions(Runnable reaper) {
        Set<String> before = localMap.keySet();
        reaper.run();
        before.removeAll(localMap.keySet());
        before.forEach(this::onSessionExpired);
    }

    protected void onSessionExpired(String sessionId) {
        try {
            expirationHandler.handle(Future.succeededFuture(sessionId));
        } catch (Exception ex) {
            expirationHandler.handle(Future.failedFuture(ex));
        }
    }
}
