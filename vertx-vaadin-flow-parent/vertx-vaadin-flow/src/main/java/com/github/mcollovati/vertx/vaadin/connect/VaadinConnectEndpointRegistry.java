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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.vaadin.flow.server.connect.Endpoint;

/**
 * A registry of Vaadin Connect Endpoint beans.
 * <p>
 * Provide access to a named collection of object instances acting
 * as Vaadin Connect endpoints; object classes should be marked
 * with @{@link Endpoint} annotation.
 */
public interface VaadinConnectEndpointRegistry {

    Map<String, Object> endpoints();

    /**
     * Factory method to create a registry from the given endpoint classes.
     * <p>
     * Classes MUST have a public no arg constructor.
     *
     * @param endpoints potential endpoint types
     * @return an {@link VaadinConnectEndpointRegistry} instance of endpoints of given class set.
     */
    static VaadinConnectEndpointRegistry fromClasses(Set<Class<?>> endpoints) {
        return () -> endpoints.stream()
            .filter(clz -> clz.getAnnotation(Endpoint.class) != null)
            .map(VaadinConnectEndpointRegistry::newInstance)
            .collect(Collectors.toMap(obj -> obj.getClass().getName(), Function.identity()));
    }

    /**
     * Factory method to create a registry from the given named objects.
     *
     * @param beans potential endpoint objects.
     * @return an {@link VaadinConnectEndpointRegistry} instance of endpoints of given class set.
     */
    static VaadinConnectEndpointRegistry fromMap(Map<String, Object> beans) {
        Map<String, Object> validEndpoints = new HashMap<>();
        beans.entrySet().stream()
            .filter(kv -> kv.getValue().getClass().getAnnotation(Endpoint.class) != null)
            .forEach(kv -> validEndpoints.put(kv.getKey(), kv.getValue()));
        return () -> validEndpoints;
    }

    /**
     * Factory method to create a registry from the given named objects supplier.
     *
     * @param beansSupplier endpoint beans supplier.
     * @return an {@link VaadinConnectEndpointRegistry} instance of endpoints provided by the given supplier.
     */
    static VaadinConnectEndpointRegistry fromSupplier(Supplier<Map<String, Object>> beansSupplier) {
        return beansSupplier::get;
    }

    static Object newInstance(Class<?> cl) {
        try {
            return cl.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an empty registry.
     *
     * @return an empty registry.
     */
    static VaadinConnectEndpointRegistry empty() {
        return Collections::emptyMap;
    }

}
