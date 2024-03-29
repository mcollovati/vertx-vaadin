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

import java.util.Set;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.startup.ClassLoaderAwareServletContainerInitializer;
import dev.hilla.Endpoint;
import dev.hilla.EndpointNameChecker;

import com.github.mcollovati.vertx.support.HillaWorkAround;

@HandlesTypes({Endpoint.class})
public class VertxEndpointRegistryInitializer implements ClassLoaderAwareServletContainerInitializer {

    @Override
    public void process(Set<Class<?>> set, ServletContext ctx) throws ServletException {
        VaadinServletContext vaadinServletContext = new VaadinServletContext(ctx);
        if (set == null || !Boolean.parseBoolean(vaadinServletContext.getContextParameter("hilla.enabled"))) {
            return;
        }
        HillaWorkAround.install();
        ClassFinder finder = new ClassFinder.DefaultClassFinder(set);
        Set<Class<?>> endpoints = finder.getAnnotatedClasses(Endpoint.class);

        vaadinServletContext.setAttribute(VaadinEndpointRegistry.class, fromClasses(endpoints));
    }

    static VaadinEndpointRegistry fromClasses(Set<Class<?>> endpoints) {
        VaadinEndpointRegistry registry = new VertxEndpointRegistry(new EndpointNameChecker());
        endpoints.stream().map(VertxEndpointRegistryInitializer::newInstance).forEach(registry::registerEndpoint);
        return registry;
    }

    static Object newInstance(Class<?> cl) {
        try {
            return cl.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
