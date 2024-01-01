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
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.BeanManager;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

/**
 * UIScopedContext is the context for {@link NormalUIScoped @NormalUIScoped}
 * beans.
 */
public class UIScopedContext extends AbstractContext {

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        return BeanProvider.getContextualReference(getBeanManager(), ContextualStorageManager.class, false)
                .getContextualStorage(createIfNotExist);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return NormalUIScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null && UI.getCurrent() != null;
    }

    /**
     * Gets a bean manager.
     * <p>
     * Not a private for testing purposes only.
     *
     * @return a bean manager
     */
    BeanManager getBeanManager() {
        return Arc.container().beanManager();
    }

    @VaadinSessionScoped
    @Unremovable
    public static class ContextualStorageManager extends AbstractContextualStorageManager<Integer> {

        public ContextualStorageManager() {
            // Session lock checked in VaadinSessionScopedContext while
            // getting the session attribute of this beans context.
            super(false);
        }

        public ContextualStorage getContextualStorage(boolean createIfNotExist) {
            final Integer uiId = UI.getCurrent().getUIId();
            return super.getContextualStorage(uiId, createIfNotExist);
        }

        @Override
        protected ContextualStorage newContextualStorage(Integer uiId) {
            UI.getCurrent().addDetachListener(this::destroy);
            return super.newContextualStorage(uiId);
        }

        private void destroy(DetachEvent event) {
            final int uiId = event.getUI().getUIId();
            super.destroy(uiId);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public ContextState getState() {
        return super.getState();
    }
}
