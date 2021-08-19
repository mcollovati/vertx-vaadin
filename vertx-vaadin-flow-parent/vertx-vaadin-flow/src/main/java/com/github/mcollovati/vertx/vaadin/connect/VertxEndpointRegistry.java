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

import com.vaadin.flow.server.connect.EndpointNameChecker;
import com.vaadin.flow.server.connect.EndpointRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class VertxEndpointRegistry extends EndpointRegistry implements VaadinEndpointRegistry {

    private final MethodHandle registerEndpointMethod;
    private final MethodHandle getMethod;

    /**
     * Creates a new registry using the given name checker.
     *
     * @param endpointNameChecker the endpoint name checker to verify custom Vaadin endpoint
     *                            names
     */
    public VertxEndpointRegistry(EndpointNameChecker endpointNameChecker) {
        super(endpointNameChecker);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Method method = EndpointRegistry.class.getDeclaredMethod("registerEndpoint", Object.class);
            method.setAccessible(true);
            registerEndpointMethod = lookup.unreflect(method).bindTo(this);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot access base class 'registerEndpoint' method");
        }
        try {
            Method method = EndpointRegistry.class.getDeclaredMethod("get", String.class);
            method.setAccessible(true);
            getMethod = lookup.unreflect(method).bindTo(this);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot access base class 'get' method");
        }
    }

    public void registerEndpoint(Object endpointBean) {
        try {
            registerEndpointMethod.invoke(endpointBean);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot invoke base class 'registerEndpoint' method", e);
        }
    }

    public EndpointRegistry.VaadinEndpointData get(String endpointName) {
        try {
            return (EndpointRegistry.VaadinEndpointData) getMethod.invoke(endpointName);
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot invoke base class 'registerEndpoint' method");
        }
    }

}
