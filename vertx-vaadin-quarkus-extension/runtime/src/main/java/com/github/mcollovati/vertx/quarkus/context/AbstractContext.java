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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InjectableContext;

/**
 * A modified copy of org.apache.deltaspike.core.util.context.AbstractContext.
 *
 * A skeleton containing the most important parts of a custom CDI Context. An
 * implementing Context needs to implement the missing methods from the
 * {@link AlterableContext} interface and
 * {@link #getContextualStorage(Contextual, boolean)}.
 */
public abstract class AbstractContext implements InjectableContext {

    /**
     * An implementation has to return the underlying storage which contains the
     * items held in the Context.
     *
     * @param contextual
     *            the contextual type
     * @param createIfNotExist
     *            whether a ContextualStorage shall get created if it doesn't
     *            yet exist.
     * @return the underlying storage
     */
    protected abstract ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist);

    /**
     * Gets all active contextual storages.
     *
     * @return a list of contextual storages.
     */
    protected List<ContextualStorage> getActiveContextualStorages() {
        List<ContextualStorage> result = new ArrayList<ContextualStorage>();
        result.add(getContextualStorage(null, false));
        return result;
    }

    @Override
    public <T> T get(Contextual<T> bean) {
        checkActive();

        ContextualStorage storage = getContextualStorage(bean, false);
        if (storage == null) {
            return null;
        }

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));
        if (contextualInstanceInfo == null) {
            return null;
        }

        return (T) contextualInstanceInfo.getContextualInstance();
    }

    @Override
    public <T> T get(Contextual<T> bean, CreationalContext<T> creationalContext) {
        if (creationalContext == null) {
            return get(bean);
        }

        checkActive();

        ContextualStorage storage = getContextualStorage(bean, true);

        Map<Object, ContextualInstanceInfo<?>> contextMap = storage.getStorage();
        ContextualInstanceInfo<?> contextualInstanceInfo = contextMap.get(storage.getBeanKey(bean));

        if (contextualInstanceInfo != null) {
            @SuppressWarnings("unchecked")
            final T instance = (T) contextualInstanceInfo.getContextualInstance();

            if (instance != null) {
                return instance;
            }
        }

        return storage.createContextualInstance(bean, creationalContext);
    }

    /**
     * Destroy the Contextual Instance of the given Bean.
     *
     * @param bean
     *            dictates which bean shall get cleaned up
     */
    @Override
    public void destroy(Contextual<?> bean) {
        ContextualStorage storage = getContextualStorage(bean, false);
        if (storage == null) {
            return;
        }

        ContextualInstanceInfo<?> contextualInstanceInfo = storage.getStorage().remove(storage.getBeanKey(bean));

        if (contextualInstanceInfo == null) {
            return;
        }

        destroyBean(bean, contextualInstanceInfo);
    }

    /**
     * destroys all the Contextual Instances in the Storage returned by
     * {@link #getContextualStorage(Contextual, boolean)}.
     */
    public void destroyAllActive() {
        List<ContextualStorage> storages = getActiveContextualStorages();
        if (storages == null) {
            return;
        }

        for (ContextualStorage storage : storages) {
            if (storage != null) {
                destroyAllActive(storage);
            }
        }
    }

    /**
     * Destroys all the Contextual Instances in the specified ContextualStorage.
     * This is a static method to allow various holder objects to cleanup
     * properly in &#064;PreDestroy.
     *
     * @param storage
     *            a contextual storage
     * @return a storage map of destroyed objects
     */
    public static Map<Object, ContextualInstanceInfo<?>> destroyAllActive(ContextualStorage storage) {
        // drop all entries in the storage before starting with destroying the
        // original entries
        Map<Object, ContextualInstanceInfo<?>> contextMap =
                new HashMap<Object, ContextualInstanceInfo<?>>(storage.getStorage());
        storage.getStorage().clear();

        for (Map.Entry<Object, ContextualInstanceInfo<?>> entry : contextMap.entrySet()) {
            Contextual<?> bean = storage.getBean(entry.getKey());

            ContextualInstanceInfo<?> contextualInstanceInfo = entry.getValue();
            destroyBean(bean, contextualInstanceInfo);
        }
        return contextMap;
    }

    @Override
    public void destroy() {
        destroyAllActive();
    }

    @Override
    public ContextState getState() {
        return this::getContextualInstances;
    }

    /**
     * Make sure that the Context is really active.
     *
     * @throws ContextNotActiveException
     *             if there is no active Context for the current Thread.
     */
    protected void checkActive() {
        if (!isActive()) {
            throw new ContextNotActiveException("CDI context with scope annotation @"
                    + getScope().getName() + " is not active with respect to the current thread");
        }
    }

    private Map<InjectableBean<?>, Object> getContextualInstances() {
        List<ContextualStorage> storages = getActiveContextualStorages();
        if (storages == null) {
            return Collections.emptyMap();
        }
        Map<InjectableBean<?>, Object> state = new HashMap<>();
        for (ContextualStorage storage : storages) {
            for (Map.Entry<Object, ContextualInstanceInfo<?>> entry :
                    storage.getStorage().entrySet()) {
                state.put(
                        (InjectableBean<?>) storage.getBean(entry.getKey()),
                        entry.getValue().getContextualInstance());
            }
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void destroyBean(Contextual bean, ContextualInstanceInfo<?> contextualInstanceInfo) {
        bean.destroy(contextualInstanceInfo.getContextualInstance(), contextualInstanceInfo.getCreationalContext());
    }
}
