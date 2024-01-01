/*
 * The MIT License
 * Copyright © 2000-2022 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.utils;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePathProvider;
import io.vertx.core.Context;
import org.mockito.Mockito;

import com.github.mcollovati.vertx.vaadin.VertxVaadinContext;

public class MockVaadinContext extends VertxVaadinContext {

    private Lookup lookup = Mockito.mock(Lookup.class);

    public static class RoutePathProviderImpl implements RoutePathProvider {

        @Override
        public String getRoutePath(Class<?> navigationTarget) {
            Route route = navigationTarget.getAnnotation(Route.class);
            return route.value();
        }
    }

    public MockVaadinContext() {
        this(Mockito.mock(Context.class), new RoutePathProviderImpl());
    }

    public MockVaadinContext(RoutePathProvider provider) {
        this(Mockito.mock(Context.class), provider);
    }

    public MockVaadinContext(Context context) {
        this(context, new RoutePathProviderImpl());
    }

    public MockVaadinContext(Context context, RoutePathProvider provider) {
        super(context);

        Mockito.when(lookup.lookup(RoutePathProvider.class)).thenReturn(null);

        Mockito.when(lookup.lookup(RoutePathProvider.class)).thenReturn(provider);

        setAttribute(lookup);
    }

    @Override
    public <T> T getAttribute(Class<T> type) {
        if (type.equals(Lookup.class)) {
            return type.cast(lookup);
        }
        return super.getAttribute(type);
    }
}
