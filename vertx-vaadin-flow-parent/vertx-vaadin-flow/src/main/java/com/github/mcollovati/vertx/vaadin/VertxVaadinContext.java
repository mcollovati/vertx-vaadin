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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.vaadin.flow.server.VaadinConfig;
import com.vaadin.flow.server.VaadinContext;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class VertxVaadinContext implements VaadinContext {

    private transient final Context context;
    private transient final Vertx vertx;
    private transient final Properties vaadinOptions;

    public VertxVaadinContext(Vertx vertx, VaadinOptions vaadinOptions) {
        this.vertx = vertx;
        this.context = vertx.getOrCreateContext();
        this.vaadinOptions = vaadinOptions.asProperties();
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
    public <T> void setAttribute(Class<T> clazz, T value) {
        if (value == null) {
            removeAttribute(clazz);
        } else {
            context.put(clazz.getName(), value);
        }
    }

    @Override
    public void removeAttribute(Class<?> clazz) {
        context.remove(clazz.getName());
    }

    @Override
    public Enumeration<String> getContextParameterNames() {
        return Collections.enumeration(vaadinOptions.keySet()
            .stream().map(String.class::cast)
            .collect(Collectors.toList()));
    }

    @Override
    public String getContextParameter(String name) {
        return vaadinOptions.getProperty(name);
    }
}
