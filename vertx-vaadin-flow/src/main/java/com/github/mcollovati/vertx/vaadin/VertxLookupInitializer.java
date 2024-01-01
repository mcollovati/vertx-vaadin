/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import java.util.Collection;
import java.util.Map;
import javax.servlet.ServletException;

import com.vaadin.flow.di.LookupInitializer;
import com.vaadin.flow.function.VaadinApplicationInitializationBootstrap;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.VaadinContext;

import static java.util.Collections.singletonList;

public class VertxLookupInitializer extends LookupInitializer {
    @Override
    public void initialize(
            VaadinContext context,
            Map<Class<?>, Collection<Class<?>>> services,
            VaadinApplicationInitializationBootstrap bootstrap)
            throws ServletException {
        services.put(BrowserLiveReloadAccessor.class, singletonList(VertxVaadinBrowserLiveReload.Accessor.class));
        super.initialize(context, services, bootstrap);
    }
}
