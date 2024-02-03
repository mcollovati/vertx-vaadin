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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic tests for all custom abstract contexts.
 *
 * @param <C>
 *            a context type
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
public abstract class AbstractContextTest<C extends AbstractContext> extends InjectableContextTest<C> {

    @Test
    public void destroyAllActive_beanExistsInContext_beanDestroyed() {
        createContext().activate();

        TestBean referenceA = getContext().get(contextual, creationalContext);
        referenceA.setState("hello");

        getContext().destroyAllActive();
        assertEquals(1, getDestroyedBeans().size());
    }

    @Test
    public void destroyAllActive_severalContexts_beanDestroyed() {
        UnderTestContext contextUnderTestA = createContext();
        contextUnderTestA.activate();
        TestBean referenceA = getContext().get(contextual, creationalContext);

        ContextualStorage storageA = getContext().getContextualStorage(contextual, false);

        referenceA.setState("hello");

        final UnderTestContext contextUnderTestB = createContext();
        contextUnderTestB.activate();
        TestBean referenceB = getContext().get(contextual, creationalContext);

        referenceB.setState("foo");

        ContextualStorage storageB = getContext().getContextualStorage(contextual, false);

        assertEquals(2, getCreatedBeansCount());

        AbstractContext.destroyAllActive(storageA);
        assertEquals(1, getDestroyedBeans().size());

        AbstractContext.destroyAllActive(storageB);
        assertEquals(2, getDestroyedBeans().size());
    }
}
