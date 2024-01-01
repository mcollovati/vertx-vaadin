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
package com.github.mcollovati.vertx.web;

import io.vertx.core.Handler;
import io.vertx.ext.web.Session;

/**
 * Created by marco on 27/07/16.
 */
public interface ExtendedSession extends Session {

    /**
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return a <code>long</code> specifying when this session was created, expressed in
     * milliseconds since 1/1/1970 GMT
     */
    long createdAt();

    /**
     * Add a handler that will be called after session expires.
     *
     * @param handler the handler
     * @return the id of the handler. This can be used if you later want to remove the handler.
     */
    int addExpiredHandler(Handler<ExtendedSession> handler);

    /**
     * Remove a session espired handler
     *
     * @param handlerID the id as returned from {@link #addExpiredHandler(Handler)}.
     * @return true if the handler existed and was removed, false otherwise
     */
    boolean removeHeadersEndHandler(int handlerID);

    static ExtendedSession adapt(Session session) {
        if (session instanceof ExtendedSession) {
            return (ExtendedSession) session;
        }
        return new ExtendedSessionImpl(session);
    }
}
