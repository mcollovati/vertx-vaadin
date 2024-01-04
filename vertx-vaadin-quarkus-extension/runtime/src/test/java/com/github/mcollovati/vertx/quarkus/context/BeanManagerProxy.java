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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import com.vaadin.flow.server.VaadinSession;
import org.mockito.Mockito;

/**
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
class BeanManagerProxy implements InvocationHandler {

    private BeanManager delegate;

    private final Bean<?> fakeBean = Mockito.mock(Bean.class);

    private final CreationalContext<?> fakeContext = Mockito.mock(CreationalContext.class);

    private final Class<?> mockingBeanType;

    BeanManagerProxy(BeanManager delegate, Class<?> mockingBeanType) {
        this.delegate = delegate;
        this.mockingBeanType = mockingBeanType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getBeans") && args[0].equals(mockingBeanType)) {
            Set<Bean<?>> set = Collections.singleton(fakeBean);
            return set;
        }
        if (method.getName().equals("resolve") && args[0].equals(Collections.singleton(fakeBean))) {
            return fakeBean;
        }
        if (method.getName().equals("createCreationalContext") && args[0].equals(fakeBean)) {
            return fakeContext;
        }
        if (method.getName().equals("getReference")
                && args[0].equals(fakeBean)
                && args[1].equals(mockingBeanType)
                && args[2].equals(fakeContext)) {
            VaadinSession session = VaadinSession.getCurrent();
            String attribute = getAttributeName(mockingBeanType);
            Object value = session.getAttribute(attribute);
            if (value == null) {
                value = BeanProvider.getContextualReference(delegate, mockingBeanType, false);
                session.setAttribute(attribute, value);
            }
            return value;
        }

        return delegate(method, args);
    }

    private Object delegate(Method method, Object[] args) throws Throwable {
        Method[] methods = delegate.getClass().getDeclaredMethods();
        List<Method> filtered = Stream.of(methods)
                .filter(origMethod -> origMethod.getName().equals(method.getName()))
                .collect(Collectors.toList());

        Method found = null;
        if (filtered.size() == 1) {
            found = filtered.get(0);
        } else {
            for (Method overloaded : filtered) {
                if (overloaded.getParameterCount() == method.getParameterCount()) {
                    found = overloaded;
                    break;
                }
            }
        }
        return found.invoke(delegate, args);
    }

    static String getAttributeName(Class<?> mockingBeanType) {
        return "test-" + mockingBeanType;
    }
}
