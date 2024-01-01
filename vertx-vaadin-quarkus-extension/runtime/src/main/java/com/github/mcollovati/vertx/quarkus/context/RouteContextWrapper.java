/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus.context;

import java.lang.annotation.Annotation;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableContext;

import com.github.mcollovati.vertx.quarkus.annotation.NormalRouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;

/**
 * Used to bind multiple scope annotations to a single context. Will delegate
 * all context-related operations to it's underlying instance, apart from
 * getting the scope of the context.
 *
 */
public class RouteContextWrapper implements InjectableContext {

    @Override
    public Class<? extends Annotation> getScope() {
        return RouteScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> component, final CreationalContext<T> creationalContext) {
        return getContext().get(component, creationalContext);
    }

    @Override
    public <T> T get(final Contextual<T> component) {
        return getContext().get(component);
    }

    @Override
    public boolean isActive() {
        return getContext().isActive();
    }

    @Override
    public void destroy(final Contextual<?> contextual) {
        getContext().destroy(contextual);
    }

    @Override
    public void destroy() {
        getContext().destroy();
    }

    @Override
    public ContextState getState() {
        return getContext().getState();
    }

    /**
     * Gets a delegating context.
     * <p>
     * Not a private for testing purposes only.
     *
     * @return a delegating context
     */
    InjectableContext getContext() {
        return Arc.container().getActiveContext(NormalRouteScoped.class);
    }
}
