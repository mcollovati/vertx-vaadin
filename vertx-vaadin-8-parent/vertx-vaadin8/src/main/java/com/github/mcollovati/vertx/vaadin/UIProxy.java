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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import com.vaadin.ui.UI;

public class UIProxy {

    private final UI ui;
    private final VertxVaadinService service;

    public UIProxy(UI ui) {
        this.ui = ui;
        this.service = (VertxVaadinService) ui.getSession().getService();
    }


    public Future<Void> runLater(UIRunnable task) {
        return schedule(task);
    }

    public <T> Future<T> runLater(UITask<T> task) {
        return schedule(task);
    }

    private <T> Future<T> schedule(UITask<T> task) {
        CompletableFuture<T> f = new CompletableFuture<>();
        service.getVertx().createSharedWorkerExecutor("vaadin.background.worker")
            .executeBlocking(completer -> {
                try {
                    completer.complete(task.execute(ui));
                } catch (Exception ex) {
                    completer.fail(ex);
                }
            }, false, res -> {
                if (res.succeeded()) {
                    f.complete(null);
                } else {
                    f.completeExceptionally(res.cause());
                }
            });
        return f;
    }

    @FunctionalInterface
    public interface UITask<T> {
        T execute(UI ui) throws Exception;
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
