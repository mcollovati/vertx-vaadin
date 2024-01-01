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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * This Storage holds all information needed for storing Contextual Instances in
 * a Context.
 *
 * It also addresses Serialisation in case of passivating scopes.
 */
public class ContextualStorage implements Serializable {

    private final Map<Object, ContextualInstanceInfo<?>> contextualInstances;

    private final boolean concurrent;

    /**
     * Creates a new instance of storage.
     *
     * @param concurrent
     *            whether the ContextualStorage might get accessed concurrently
     *            by different threads
     */
    public ContextualStorage(boolean concurrent) {
        this.concurrent = concurrent;
        if (concurrent) {
            contextualInstances = new ConcurrentHashMap<Object, ContextualInstanceInfo<?>>();
        } else {
            contextualInstances = new HashMap<Object, ContextualInstanceInfo<?>>();
        }
    }

    /**
     * @return the underlying storage map.
     */
    public Map<Object, ContextualInstanceInfo<?>> getStorage() {
        return contextualInstances;
    }

    /**
     * @return whether the ContextualStorage might get accessed concurrently by
     *         different threads.
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     *
     * @param bean
     *            the contextual type
     * @param creationalContext
     *            a context
     * @param <T>
     *            contextual instance type
     * @return a created contextual instance
     */
    public <T> T createContextualInstance(Contextual<T> bean, CreationalContext<T> creationalContext) {
        Object beanKey = getBeanKey(bean);
        if (isConcurrent()) {
            // locked approach
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<T>();

            ConcurrentMap<Object, ContextualInstanceInfo<?>> concurrentMap =
                    (ConcurrentHashMap<Object, ContextualInstanceInfo<?>>) contextualInstances;

            ContextualInstanceInfo<T> oldInstanceInfo =
                    (ContextualInstanceInfo<T>) concurrentMap.putIfAbsent(beanKey, instanceInfo);

            if (oldInstanceInfo != null) {
                instanceInfo = oldInstanceInfo;
            }
            synchronized (instanceInfo) {
                T instance = instanceInfo.getContextualInstance();
                if (instance == null) {
                    instance = bean.create(creationalContext);
                    instanceInfo.setContextualInstance(instance);
                    instanceInfo.setCreationalContext(creationalContext);
                }

                return instance;
            }

        } else {
            // simply create the contextual instance
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<T>();
            instanceInfo.setCreationalContext(creationalContext);
            instanceInfo.setContextualInstance(bean.create(creationalContext));

            contextualInstances.put(beanKey, instanceInfo);

            return instanceInfo.getContextualInstance();
        }
    }

    /**
     * If the context is a passivating scope then we return the passivationId of
     * the Bean. Otherwise we use the Bean directly.
     *
     * @param <T>
     *            bean type
     * @param bean
     *            the contextual type
     *
     * @return the key to use in the context map
     */
    public <T> Object getBeanKey(Contextual<T> bean) {
        return bean;
    }

    /**
     * Restores the Bean from its beanKey.
     *
     * @see #getBeanKey(Contextual)
     *
     * @param beanKey
     *            a bean key
     *
     * @return the contextual type
     */
    public Contextual<?> getBean(Object beanKey) {
        return (Contextual<?>) beanKey;
    }
}
