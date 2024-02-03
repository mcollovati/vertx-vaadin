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

import java.lang.reflect.Proxy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.BeanManager;

import io.quarkus.arc.Unremovable;
import io.quarkus.test.junit.QuarkusTest;

import com.github.mcollovati.vertx.quarkus.context.UiContextTest.TestUIScopedContext;

/*
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */

@QuarkusTest
public class UiContextTest extends AbstractContextTest<TestUIScopedContext> {

    @Override
    protected UnderTestContext newContextUnderTest() {
        return new UIUnderTestContext();
    }

    @Override
    protected Class<TestUIScopedContext> getContextType() {
        return TestUIScopedContext.class;
    }

    public static class TestUIScopedContext extends UIScopedContext {

        @Override
        BeanManager getBeanManager() {
            BeanManager beanManager = super.getBeanManager();
            BeanManager proxy = (BeanManager) Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] {BeanManager.class},
                    new BeanManagerProxy(beanManager, UIScopedContext.ContextualStorageManager.class));
            return proxy;
        }
    }

    @Dependent
    @Unremovable
    @Alternative
    @Priority(1)
    public static class TestContextualStorageManager extends UIScopedContext.ContextualStorageManager {}
}
