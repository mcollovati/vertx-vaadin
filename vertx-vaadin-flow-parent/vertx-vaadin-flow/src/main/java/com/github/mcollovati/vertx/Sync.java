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
package com.github.mcollovati.vertx;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by marco on 22/07/16.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Sync {

    public static <T> T await(Consumer<Handler<AsyncResult<T>>> task) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Future<T> f = Future.<T>future().setHandler(ar -> {
                countDownLatch.countDown();
                if (ar.failed()) {
                    throw new VertxException(ar.cause());
                }
            });
            task.accept(f.completer());
            countDownLatch.await();
            return f.result();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VertxException(e);
        }
    }


}
