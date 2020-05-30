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
package com.github.mcollovati.vertx.vaadin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.Command;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIProxy {

    private static final Logger logger = LoggerFactory.getLogger(UIProxy.class);
    private final UI ui;
    private final VertxVaadinService service;
    private final WorkerExecutor executor;

    public UIProxy(UI ui) {
        this.ui = ui;
        service = (VertxVaadinService) ui.getSession().getService();
        executor = service.getVertx().createSharedWorkerExecutor("vaadin.background.worker");
    }


    public Future<Void> runLater(UIRunnable task) {
        return schedule(task);
    }

    public <T> Future<T> runLater(UITask<T> task) {
        return schedule(task);
    }

    public static io.vertx.core.Future<Void> accessAndWait(UI ui, Command command) {
        Promise<Void> p = Promise.promise();
        ui.access(() -> {
            try {
                command.execute();
                p.complete();
            } catch (Exception ex) {
                logger.error("Error waiting for UI.access to complete", ex);
                p.fail(ex);
            }
        });
        return p.future();
    }

    public Future<Void> schedule(UIRunnable task, long delay, TimeUnit unit) {
        return schedule((UITask<Void>) task, delay, unit);
    }

    public <T> Future<T> schedule(UITask<T> task, long delay, TimeUnit unit) {
        CompletableFuture<T> f = new CompletableFuture<>();
        service.getVertx().setTimer(unit.toMillis(delay), timerId -> {
            Promise<T> promise = Promise.promise();
            executeTask(task).handle(promise);
            completeFuture(f).handle(promise.future());
        });
        return f;
    }

    public void close() {
        executor.close();
    }

    private <T> Future<T> schedule(UITask<T> task) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.executeBlocking(executeTask(task), false, completeFuture(f));
        return f;
    }

    private <T> Handler<AsyncResult<T>> completeFuture(CompletableFuture<T> f) {
        return res -> {
            if (res.succeeded()) {
                f.complete(null);
            } else {
                f.completeExceptionally(res.cause());
            }
        };
    }

    private <T> Handler<Promise<T>> executeTask(UITask<T> task) {
        return completer -> {
            try {
                completer.complete(task.execute(ui));
            } catch (Exception ex) {
                completer.fail(ex);
            }
        };
    }

    @FunctionalInterface
    public interface UITask<T> {
        T execute(UI ui) throws Exception;

        static <T> UITask<T> fromCallable(Callable<T> callable) {
            return ui -> callable.call();
        }
    }

    @FunctionalInterface
    public interface UIRunnable extends UITask<Void> {

        void run() throws Exception;

        @Override
        default Void execute(UI ui) throws Exception {
            run();
            return null;
        }
    }

}
