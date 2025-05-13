
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

import java.util.HashSet;
import java.util.Set;

import com.vaadin.hilla.*;
import com.vaadin.hilla.parser.jackson.JacksonObjectMapperFactory;
import com.vaadin.hilla.signals.core.registry.SecureSignalsRegistry;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

import com.vaadin.flow.server.VaadinServletContext;
import com.vaadin.flow.server.frontend.scanner.ClassFinder;
import com.vaadin.flow.server.startup.ClassLoaderAwareServletContainerInitializer;

import com.github.mcollovati.vertx.support.HillaWorkAround;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.context.ApplicationContext;
import com.vaadin.hilla.signals.handler.SignalsHandler;


@HandlesTypes({Endpoint.class, BrowserCallable.class})
public class VertxEndpointRegistryInitializer implements ClassLoaderAwareServletContainerInitializer {

    ApplicationContext springContext;

    // for hilla signal endpoint instance
    SignalsHandler signal;

    private VertxEndpointRegistry endpontRegistry = new VertxEndpointRegistry(new EndpointNameChecker());

    @Override
    public void process(Set<Class<?>> set, ServletContext ctx) throws ServletException {
        VaadinServletContext vaadinServletContext = new VaadinServletContext(ctx);
        if (set == null || !Boolean.parseBoolean(vaadinServletContext.getContextParameter("hilla.enabled"))) {
            return;
        }
        springContext = (ApplicationContext)ctx.getAttribute("springContext");

        if(signal==null){
            EndpointInvoker invoker = new EndpointInvoker(springContext,
                    new JacksonObjectMapperFactory.Json().build(),
                    new ExplicitNullableTypeChecker(),
                    ctx,
                    endpontRegistry
            );
            SecureSignalsRegistry registry = new SecureSignalsRegistry(invoker);
            signal = new SignalsHandler(registry);
        }

        HillaWorkAround.install();
        ClassFinder finder = new ClassFinder.DefaultClassFinder(set);
        Set<Class<?>> endpoints = new HashSet<>();
        endpoints.addAll(finder.getAnnotatedClasses(Endpoint.class));
        endpoints.addAll(finder.getAnnotatedClasses(BrowserCallable.class));

        vaadinServletContext.setAttribute(VaadinEndpointRegistry.class, fromClasses(endpoints));
    }

    VaadinEndpointRegistry fromClasses(Set<Class<?>> endpoints) {
        VaadinEndpointRegistry registry = endpontRegistry;
        endpoints.stream().map(this::newInstance).forEach(registry::registerEndpoint);
        return registry;
    }

    final Object newInstance(Class<?> cl) {

        try {

            if(cl.equals(SignalsHandler.class)){
                return signal;
            }

            if(springContext!=null){
                try {
                    return getOrCreate(springContext, cl);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            return cl.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getOrCreate(ApplicationContext context, Class<T> type) {
        if (context.getBeanNamesForType(type).length == 1) {
            return context.getBean(type);
        } else if (context.getBeanNamesForType(type).length > 1) {
            try {
                return context.getAutowireCapableBeanFactory().createBean(type);
            } catch (BeanInstantiationException var3) {
                throw new BeanInstantiationException(var3.getBeanClass(), "[HINT] This could be caused by more than one suitable beans for autowiring in the context.", var3);
            }
        } else {
            return context.getAutowireCapableBeanFactory().createBean(type);
        }
    }
}
