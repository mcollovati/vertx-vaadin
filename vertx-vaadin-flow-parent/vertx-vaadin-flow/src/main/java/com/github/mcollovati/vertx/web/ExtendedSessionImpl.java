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
package com.github.mcollovati.vertx.web;

import java.util.HashMap;
import java.util.Map;

import com.github.mcollovati.vertx.web.serialization.SerializableHolder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;

/**
 * Created by marco on 27/07/16.
 */
public class ExtendedSessionImpl extends SharedDataSessionImpl implements ExtendedSession, Shareable, ClusterSerializable {


    protected Session delegate;
    private long createdAt;

    public ExtendedSessionImpl() {
        this.delegate = new SharedDataSessionImpl();
    }

    public ExtendedSessionImpl(Session delegate) {
        this.delegate = delegate;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public int addExpiredHandler(Handler<ExtendedSession> handler) {
        return 0;
    }

    @Override
    public boolean removeHeadersEndHandler(int handlerID) {
        return false;
    }

    @Override
    public String id() {
        return delegate.id();
    }

    @Override
    public Session put(String key, Object obj) {
        return delegate.put(key, wrapIfNeeded(obj));
    }

    @Override
    public <T> T get(String key) {
        return unwrapIfNeeded(delegate.get(key));
    }

    @Override
    public <T> T remove(String key) {
        return unwrapIfNeeded(delegate.remove(key));
    }

    @Override
    public Map<String, Object> data() {
        Map<String, Object> copy = new HashMap<>(delegate.data());
        copy.replaceAll( (key, obj) -> unwrapIfNeeded(obj));
        return copy;
    }

    @Override
    public long lastAccessed() {
        return delegate.lastAccessed();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return delegate.isDestroyed();
    }

    @Override
    public long timeout() {
        return delegate.timeout();
    }

    @Override
    public void setAccessed() {
        delegate.setAccessed();
    }

    @Override
    public long createdAt() {
        return createdAt;
    }

    @Override
    public Session regenerateId() {
        this.createdAt = System.currentTimeMillis();
        return delegate.regenerateId();
    }

    @Override
    public boolean isRegenerated() {
        return delegate.isRegenerated();
    }

    @Override
    public String oldId() {
        return delegate.oldId();
    }

    @Override
    public void writeToBuffer(Buffer buffer) {
        buffer.appendLong(createdAt);
        ((ClusterSerializable) delegate).writeToBuffer(buffer);
    }

    @Override
    public int readFromBuffer(int pos, Buffer buffer) {
        createdAt = buffer.getLong(pos);
        return ((ClusterSerializable) delegate).readFromBuffer(pos + 8, buffer);
    }

    @SuppressWarnings("unchecked")
    private <T> T unwrapIfNeeded(T obj) {
        if (obj instanceof SerializableHolder) {
            return (T) ((SerializableHolder) obj).get();
        }
        return obj;
    }

    private Object wrapIfNeeded(Object obj) {
        if (obj == null || obj instanceof Number || obj instanceof Character || obj instanceof String
            || obj instanceof Boolean || obj instanceof ClusterSerializable) {
            return obj;
        }
        return new SerializableHolder(obj);
    }

}