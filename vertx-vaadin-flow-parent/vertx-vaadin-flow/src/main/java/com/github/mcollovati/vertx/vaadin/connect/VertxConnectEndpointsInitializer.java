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
package com.github.mcollovati.vertx.vaadin.connect;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.startup.ClassLoaderAwareServletContainerInitializer;

@HandlesTypes({Endpoint.class})
public class VertxConnectEndpointsInitializer implements ClassLoaderAwareServletContainerInitializer {

    @Override
    public void process(Set<Class<?>> set, ServletContext ctx) throws ServletException {

        if (set == null) {
            return;
        }

        ClassFinder finder = new ClassFinder.DefaultClassFinder(set);
        Set<Class<?>> endpoints = finder.getAnnotatedClasses(Endpoint.class);
        new VaadinServletContext(ctx)
            .setAttribute(VaadinConnectEndpointRegistry.class, VaadinConnectEndpointRegistry.fromClasses(endpoints));


    }
}
