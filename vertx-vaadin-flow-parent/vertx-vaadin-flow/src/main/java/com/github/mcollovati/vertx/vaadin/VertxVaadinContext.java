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
package com.github.mcollovati.vertx.vaadin;

import java.util.function.Supplier;

import com.vaadin.flow.server.VaadinContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class VertxVaadinContext implements VaadinContext {

    private transient final Context context;
    private transient final Vertx vertx;

    public VertxVaadinContext(Vertx vertx) {
        this.vertx = vertx;
        this.context = vertx.getOrCreateContext();
    }

    @Override
    public <T> T getAttribute(Class<T> type, Supplier<T> defaultValueSupplier) {
        T result = context.get(type.getName());
        if (result == null && defaultValueSupplier != null) {
            result = defaultValueSupplier.get();
            context.put(type.getName(), result);
        }
        return result;
    }

    @Override
    public <T> void setAttribute(T value) {
        assert value != null;
        context.put(value.getClass().getName(), value);
    }

    @Override
    public void removeAttribute(Class<?> clazz) {
        context.remove(clazz.getName());
    }
}
