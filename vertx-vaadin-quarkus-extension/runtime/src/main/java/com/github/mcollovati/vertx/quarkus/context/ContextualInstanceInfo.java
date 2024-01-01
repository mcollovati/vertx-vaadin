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

import java.io.Serializable;
import javax.enterprise.context.spi.CreationalContext;

/**
 * A copy of org.apache.deltaspike.core.util.context.ContextualInstanceInfo.
 *
 *
 * This data holder contains all necessary data you need to store a Contextual
 * Instance in a CDI Context.
 */
public class ContextualInstanceInfo<T> implements Serializable {

    /**
     * The actual Contextual Instance in the context
     */
    private T contextualInstance;

    /**
     * We need to store the CreationalContext as we need it for properly
     * destroying the contextual instance via
     * {@link javax.enterprise.context.spi.Contextual#destroy(Object, CreationalContext)}
     */
    private CreationalContext<T> creationalContext;

    /**
     * @return the CreationalContext of the bean
     */
    public CreationalContext<T> getCreationalContext() {
        return creationalContext;
    }

    /**
     * @param creationalContext
     *            the CreationalContext of the bean
     */
    public void setCreationalContext(CreationalContext<T> creationalContext) {
        this.creationalContext = creationalContext;
    }

    /**
     * @return the contextual instance itself
     */
    public T getContextualInstance() {
        return contextualInstance;
    }

    /**
     * @param contextualInstance
     *            the contextual instance itself
     */
    public void setContextualInstance(T contextualInstance) {
        this.contextualInstance = contextualInstance;
    }
}
