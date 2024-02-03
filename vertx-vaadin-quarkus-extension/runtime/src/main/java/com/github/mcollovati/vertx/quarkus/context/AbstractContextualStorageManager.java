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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PreDestroy;

/**
 * Base class for manage and store ContextualStorages.
 *
 * This class is responsible for - creating, and providing the ContextualStorage
 * for a context key - destroying ContextualStorages
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
abstract class AbstractContextualStorageManager<K> implements Serializable {
    private final boolean concurrent;
    private final Map<K, ContextualStorage> storageMap;

    protected AbstractContextualStorageManager(boolean concurrent) {
        if (concurrent) {
            this.storageMap = new ConcurrentHashMap<>();
        } else {
            this.storageMap = new HashMap<>();
        }
        this.concurrent = concurrent;
    }

    /**
     * Gets the {@link ContextualStorage} associated with the given context
     * {@code key}, possibly creating a new instance, if requested and not
     * already existing.
     *
     * @param key
     *            context key
     * @param createIfNotExist
     *            whether a ContextualStorage shall get created if it doesn't
     *            yet exist.
     * @return a {@link ContextualStorage} instance.
     */
    protected ContextualStorage getContextualStorage(K key, boolean createIfNotExist) {
        if (createIfNotExist) {
            return storageMap.computeIfAbsent(key, this::newContextualStorage);
        } else {
            return storageMap.get(key);
        }
    }

    /**
     * Changes the context key for a contextual storage.
     *
     * @param from
     *            the contextual storage context key
     * @param to
     *            the new context for the contextual storage
     */
    protected void relocate(K from, K to) {
        ContextualStorage storage = storageMap.remove(from);
        if (storage != null) {
            storageMap.put(to, storage);
        }
    }

    /**
     * Creates a new {@link ContextualStorage} for the given context key.
     *
     * @param key
     *            the context key
     * @return a new {@link ContextualStorage} instance.
     */
    protected ContextualStorage newContextualStorage(K key) {
        return new ContextualStorage(concurrent);
    }

    /**
     * Destroys all existing contextual storages.
     */
    @PreDestroy
    protected void destroyAll() {
        Collection<ContextualStorage> storages = storageMap.values();
        for (ContextualStorage storage : storages) {
            AbstractContext.destroyAllActive(storage);
        }
        storageMap.clear();
    }

    /**
     * Destroys the contextual storage associated with the given context key.
     *
     * @param key
     *            the context key
     */
    protected void destroy(K key) {
        ContextualStorage storage = storageMap.remove(key);
        if (storage != null) {
            AbstractContext.destroyAllActive(storage);
        }
    }

    /**
     * Gets context keys of all registered contextual storages.
     *
     * @return immutable set of context keys.
     */
    protected Set<K> getKeySet() {
        return Collections.unmodifiableSet(storageMap.keySet());
    }
}
