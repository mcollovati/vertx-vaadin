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

import io.vertx.core.Vertx;

public interface NearCacheSessionStore extends ExtendedSessionStore {
    /**
     * The default name used for the session map
     */
    String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";

    /**
     * Default retry time out, in ms, for a session not found in this store.
     */
    long DEFAULT_RETRY_TIMEOUT = 5 * 1000; // 5 seconds

    /**
     * Default of how often, in ms, to check for expired sessions
     */
    long DEFAULT_REAPER_INTERVAL = 1000;

    /**
     * Create a session store
     *
     * @param vertx          the Vert.x instance
     * @param sessionMapName the session map name
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx, String sessionMapName) {
        return new NearCacheSessionStoreImpl(vertx, sessionMapName, DEFAULT_RETRY_TIMEOUT, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store.
     *
     * The retry timeout value, configures how long the session handler will retry to get a session from the store
     * when it is not found.
     *
     * @param vertx          the Vert.x instance
     * @param sessionMapName the session map name
     * @param retryTimeout   the store retry timeout, in ms
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx, String sessionMapName, long retryTimeout) {
        return new NearCacheSessionStoreImpl(vertx, sessionMapName, retryTimeout, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store.
     *
     * The retry timeout value, configures how long the session handler will retry to get a session from the store
     * when it is not found.
     * The reaper interval configures how often, in ms, to check for expired sessions
     *
     * @param vertx          the Vert.x instance
     * @param sessionMapName the session map name
     * @param retryTimeout   the store retry timeout, in ms
     * @param reaperInterval how often, in ms, to check for expired sessions
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx, String sessionMapName, long retryTimeout, long reaperInterval) {
        return new NearCacheSessionStoreImpl(vertx, sessionMapName, retryTimeout, reaperInterval);
    }

    /**
     * Create a session store.
     *
     * @param vertx the Vert.x instance
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx) {
        return new NearCacheSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_RETRY_TIMEOUT, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store.
     *
     * The retry timeout value, configures how long the session handler will retry to get a session from the store
     * when it is not found.
     *
     * @param vertx        the Vert.x instance
     * @param retryTimeout the store retry timeout, in ms
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx, long retryTimeout) {
        return new NearCacheSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, retryTimeout, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store.
     *
     * The retry timeout value, configures how long the session handler will retry to get a session from the store
     * when it is not found.
     * The reaper interval configures how often, in ms, to check for expired sessions.
     *
     * @param vertx          the Vert.x instance
     * @param retryTimeout   the store retry timeout, in ms
     * @param reaperInterval how often, in ms, to check for expired sessions
     * @return the session store
     */
    static NearCacheSessionStore create(Vertx vertx, long retryTimeout, long reaperInterval) {
        return new NearCacheSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, retryTimeout, reaperInterval);
    }
}
