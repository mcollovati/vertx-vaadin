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

import java.lang.annotation.Annotation;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanManager;

import io.quarkus.arc.Arc;

/**
 * A modified copy of org.apache.deltaspike.core.util.ContextUtils.
 *
 * A set of utility methods for working with contexts.
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
@Typed()
public abstract class ContextUtils {
    private ContextUtils() {
        // prevent instantiation
    }

    /**
     * Checks if the context for the given scope annotation is active.
     *
     * @param scopeAnnotationClass
     *            The scope annotation (e.g. @RequestScoped.class)
     * @return If the context is active.
     */
    public static boolean isContextActive(Class<? extends Annotation> scopeAnnotationClass) {
        return isContextActive(scopeAnnotationClass, Arc.container().beanManager());
    }

    /**
     * Checks if the context for the given scope annotation is active.
     *
     * @param scopeAnnotationClass
     *            The scope annotation (e.g. @RequestScoped.class)
     * @param beanManager
     *            The {@link BeanManager}
     * @return If the context is active.
     */
    public static boolean isContextActive(Class<? extends Annotation> scopeAnnotationClass, BeanManager beanManager) {
        try {
            if (beanManager.getContext(scopeAnnotationClass) == null
                    || !beanManager.getContext(scopeAnnotationClass).isActive()) {
                return false;
            }
        } catch (ContextNotActiveException e) {
            return false;
        }

        return true;
    }
}
