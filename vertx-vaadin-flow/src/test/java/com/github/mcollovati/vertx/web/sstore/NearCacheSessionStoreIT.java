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

import java.io.Serializable;
import java.util.Objects;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mcollovati.vertx.Sync;
import com.github.mcollovati.vertx.web.ExtendedSession;

import static io.vertx.ext.web.sstore.SessionStore.DEFAULT_SESSIONID_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class NearCacheSessionStoreIT {

    public static final int DEFAULT_TIMEOUT = 30 * 60 * 1000;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext(() -> Sync.await(completer ->
            Vertx.clusteredVertx(new VertxOptions().setClusterManager(new HazelcastClusterManager()), completer)));

    private LocalMap<String, Session> localMap;
    private AsyncMap<String, Session> remoteMap;

    @Before
    public void setup(TestContext context) {
        getRemoteMap(context, context.asyncAssertSuccess());
        localMap = getLocalMap();
    }

    private void getRemoteMap(TestContext context, Handler<AsyncResult<AsyncMap<String, Session>>> resultHandler) {
        if (remoteMap == null) {
            rule.vertx()
                    .sharedData()
                    .<String, Session>getClusterWideMap(NearCacheSessionStore.DEFAULT_SESSION_MAP_NAME, res -> {
                        if (res.succeeded()) {
                            remoteMap = res.result();
                            resultHandler.handle(Future.succeededFuture(res.result()));
                        } else {
                            resultHandler.handle(res);
                        }
                    });
        } else {
            resultHandler.handle(Future.succeededFuture(remoteMap));
        }
    }

    private void doWithRemoteSession(TestContext context, Session session, Handler<AsyncResult<Session>> handler) {
        remoteMap.get(session.id(), handler);
    }

    private void doWithLocalSession(TestContext context, Session session, Handler<AsyncResult<Session>> handler) {
        try {
            Session localSession = localMap.get(session.id());
            handler.handle(Future.succeededFuture(localSession));
        } catch (Exception ex) {
            handler.handle(Future.failedFuture(ex));
        }
    }

    private LocalMap<String, Session> getLocalMap() {
        return rule.vertx().sharedData().getLocalMap(NearCacheSessionStore.DEFAULT_SESSION_MAP_NAME);
    }

    @Test
    public void createSession(TestContext context) {

        Vertx vertx = rule.vertx();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        long beforeCreationTime = System.currentTimeMillis();
        Session session = sessionStore.createSession(3600);
        assertThat(session.id()).isNotEmpty();
        assertThat(session.timeout()).isEqualTo(3600);
        assertThat(session.lastAccessed()).isCloseTo(beforeCreationTime, Offset.offset(100L));
        assertThat(session.isDestroyed()).isFalse();
    }

    @Test(timeout = 3000)
    public void putShouldUpdateRemoteSession(TestContext context) {
        Vertx vertx = rule.vertx();

        TestObject testObject = new TestObject("TestObject");
        ExtendedSession session = createSession(vertx);
        String testObjKey = "testObjKey";
        session.put(testObjKey, testObject);

        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        sessionStore.put(session, context.asyncAssertSuccess(b -> {
            doWithRemoteSession(
                    context,
                    session,
                    context.asyncAssertSuccess(s -> context.verify(unused -> assertSessionProperties(session, s))));
            doWithLocalSession(
                    context,
                    session,
                    context.asyncAssertSuccess(s -> context.verify(unused -> {
                        assertSessionProperties(session, s);
                    })));
        }));
    }

    @Test(timeout = 3000)
    public void consecutiveGetAndPutShouldBeConsistent(TestContext context) {
        Vertx vertx = rule.vertx();

        TestObject testObject = new TestObject("TestObject");
        ExtendedSession session = createSession(vertx);
        String testObjKey = "testObjKey";
        session.put(testObjKey, testObject);

        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        sessionStore.put(session, context.asyncAssertSuccess(x -> {
            sessionStore.get(session.id(), context.asyncAssertSuccess(freshSession -> {
                sessionStore.put(freshSession, context.asyncAssertSuccess());
            }));
        }));
    }

    @Test(timeout = 5000)
    public void getShouldReturnLocalSessionIfPresent(TestContext context) {
        Vertx vertx = rule.vertx();
        TestObject testObject = new TestObject("TestObject");
        ExtendedSession session = createSession(vertx);
        String testObjKey = "testObjKey";
        session.put(testObjKey, testObject);
        localMap.put(session.id(), session);

        SessionStore sessionStore = NearCacheSessionStore.create(vertx);

        sessionStore.get(session.id(), context.asyncAssertSuccess(s -> {
            assertSessionProperties(session, s);
            assertThat(s.<TestObject>get(testObjKey)).isSameAs(testObject);
        }));
    }

    @Test(timeout = 5000)
    public void getShouldReturnRemoteSessionIfLocalIsMissingPresent(TestContext context) {
        Vertx vertx = rule.vertx();
        TestObject testObject = new TestObject("TestObject");
        ExtendedSession session = createSession(vertx);
        String testObjKey = "testObjKey";
        session.put(testObjKey, testObject);
        remoteMap.put(session.id(), session, context.asyncAssertSuccess());

        SessionStore sessionStore = NearCacheSessionStore.create(vertx);

        sessionStore.get(session.id(), context.asyncAssertSuccess(s -> {
            assertSessionProperties(session, s);
            assertThat(s.<TestObject>get(testObjKey)).isNotSameAs(testObject).isEqualTo(testObject);
        }));
    }

    @Test(timeout = 5000)
    public void deleteShouldSucceedIfSessionDoesNotExist(TestContext context) {
        Vertx vertx = rule.vertx();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        sessionStore.delete("XY", context.asyncAssertSuccess());
    }

    @Test(timeout = 5000)
    public void deleteShouldRemoveSessionFromLocalAndRemote(TestContext context) {
        Vertx vertx = rule.vertx();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        Session session = sessionStore.createSession(DEFAULT_TIMEOUT);
        TestObject testObject = new TestObject("TestObject");
        session.put("TEST_KEY", testObject);

        sessionStore.delete("XY", context.asyncAssertSuccess(u -> {
            doWithLocalSession(context, session, context.asyncAssertSuccess(context::assertNull));
            doWithRemoteSession(context, session, context.asyncAssertSuccess(context::assertNull));
        }));
    }

    @Test(timeout = 5000)
    public void clearShouldSucceedIfSessionDoesNotExist(TestContext context) {
        Vertx vertx = rule.vertx();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        sessionStore.clear(context.asyncAssertSuccess());
    }

    @Test(timeout = 5000)
    public void clearShouldEmptyLocalAndRemoteSession(TestContext context) {
        Vertx vertx = rule.vertx();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        Session session = sessionStore.createSession(DEFAULT_TIMEOUT);
        TestObject testObject = new TestObject("TestObject");
        session.put("TEST_KEY", testObject);

        sessionStore.clear(context.asyncAssertSuccess(u -> {
            context.assertTrue(localMap.isEmpty(), "Local map should be empty");
            remoteMap.size(
                    context.asyncAssertSuccess(size -> context.assertTrue(size == 0, "Remote map should be empty")));
        }));
    }

    @Test
    public void storeShouldRemoveExpiredSessionFromLocalAndRemote(TestContext context) {
        Vertx vertx = rule.vertx();
        Async async = context.async();
        SessionStore sessionStore = NearCacheSessionStore.create(vertx);
        Session session = sessionStore.createSession(3000);
        sessionStore.put(session, context.asyncAssertSuccess());

        vertx.setTimer(5000, unused -> {
            sessionStore.get("XY", context.asyncAssertSuccess(u -> {
                doWithRemoteSession(
                        context,
                        session,
                        context.asyncAssertSuccess(s -> context.assertNull(s, "Remote session should not be present")));
                doWithLocalSession(
                        context,
                        session,
                        context.asyncAssertSuccess(s -> context.assertNull(s, "Local session should not be present")));
            }));
            async.complete();
        });
    }

    @Test(timeout = 5000)
    public void storeShouldFireExpirationEvent(TestContext context) {
        Vertx vertx = rule.vertx();
        Async async = context.async(2);
        NearCacheSessionStore sessionStore = NearCacheSessionStore.create(vertx);
        sessionStore.expirationHandler(res -> async.countDown());

        Session session = sessionStore.createSession(1000);
        sessionStore.put(session, context.asyncAssertSuccess());

        Session session2 = sessionStore.createSession(3000);
        sessionStore.put(session2, context.asyncAssertSuccess());
    }

    private ExtendedSession createSession(Vertx vertx) {
        return ExtendedSession.adapt(new SharedDataSessionImpl(new PRNG(vertx), 36000, DEFAULT_SESSIONID_LENGTH));
    }

    private void assertSessionProperties(ExtendedSession session, Session rs) {
        assertThat(rs.lastAccessed()).isEqualTo(session.lastAccessed());
        assertThat(rs.id()).isEqualTo(session.id());
        assertThat(rs.isDestroyed()).isEqualTo(session.isDestroyed());
        assertThat(rs.timeout()).isEqualTo(session.timeout());
    }

    private static class TestObject implements Serializable {
        private final String name;
        private int counter = 0;

        public TestObject(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestObject)) return false;
            TestObject that = (TestObject) o;
            return counter == that.counter && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(counter, name);
        }
    }
}
