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

import com.github.mcollovati.vertx.web.ExtendedSession;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by marco on 27/07/16.
 */
public class ExtendedSessionUT {

    @Test
    public void extendeSessionShouldBeClusterSerializable() throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        SharedDataSessionImpl delegate = new SharedDataSessionImpl(new PRNG(vertx), 3000, SessionStore.DEFAULT_SESSIONID_LENGTH);
        ExtendedSession extendedSession = ExtendedSession.adapt(delegate);
        assertThat(extendedSession).isInstanceOf(ClusterSerializable.class);
        long createdAt = extendedSession.createdAt();
        extendedSession.put("key1", "value");
        extendedSession.put("key2", 20);
        Thread.sleep(300);

        Buffer buffer = Buffer.buffer();
        ((ClusterSerializable) extendedSession).writeToBuffer(buffer);
        assertThat(buffer.length() > 0);

        ExtendedSession fromBuffer = ExtendedSession.adapt(
            new SharedDataSessionImpl(new PRNG(vertx), 0, SessionStore.DEFAULT_SESSIONID_LENGTH)
        );
        ((ClusterSerializable) fromBuffer).readFromBuffer(0, buffer);
        assertThat(fromBuffer.createdAt()).isEqualTo(createdAt);
        assertThat(fromBuffer.id()).isEqualTo(delegate.id());
        assertThat(fromBuffer.timeout()).isEqualTo(delegate.timeout());
        assertThat(fromBuffer.data()).isEqualTo(delegate.data());

    }
}
