/*
 * The MIT License
 * Copyright © 2016-2019 Marco Collovati (mcollovati@gmail.com)
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


/**
 * A session store that extends {@link io.vertx.ext.web.sstore.LocalSessionStore}
 * with support for expiration handler
 */
public interface ExtendedLocalSessionStore extends ExtendedSessionStore {

    /**
     * Default of how often, in ms, to check for expired sessions
     */
    long DEFAULT_REAPER_INTERVAL = 1000;

    /**
     * Default name for map used to store sessions
     */
    String DEFAULT_SESSION_MAP_NAME = "vertx-web.sessions";

    /**
     * Create a session store
     *
     * @param vertx the Vert.x instance
     * @return the session store
     */
    static ExtendedLocalSessionStore create(Vertx vertx) {
        return new ExtendedLocalSessionStoreImpl(vertx, DEFAULT_SESSION_MAP_NAME, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store
     *
     * @param vertx          the Vert.x instance
     * @param sessionMapName name for map used to store sessions
     * @return the session store
     */
    static ExtendedLocalSessionStore create(Vertx vertx, String sessionMapName) {
        return new ExtendedLocalSessionStoreImpl(vertx, sessionMapName, DEFAULT_REAPER_INTERVAL);
    }

    /**
     * Create a session store
     *
     * @param vertx          the Vert.x instance
     * @param sessionMapName name for map used to store sessions
     * @param reaperInterval how often, in ms, to check for expired sessions
     * @return the session store
     */
    static ExtendedLocalSessionStore create(Vertx vertx, String sessionMapName, long reaperInterval) {
        return new ExtendedLocalSessionStoreImpl(vertx, sessionMapName, reaperInterval);
    }

}
