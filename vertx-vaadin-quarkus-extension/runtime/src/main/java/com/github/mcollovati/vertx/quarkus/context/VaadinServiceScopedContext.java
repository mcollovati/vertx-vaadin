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
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;

import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import com.github.mcollovati.vertx.quarkus.QuarkusVertxVaadinService;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceScoped;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Context for {@link VaadinServiceScoped @VaadinServiceScoped} beans.
 */
public class VaadinServiceScopedContext extends AbstractContext {

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        String name = VertxVaadinService.getCurrent().getServiceName();
        return BeanProvider.getContextualReference(Arc.container().beanManager(), ContextualStorageManager.class, false)
                .getContextualStorage(name, createIfNotExist);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return VaadinServiceScoped.class;
    }

    @Override
    public boolean isActive() {
        VaadinService service = VaadinService.getCurrent();
        return service instanceof QuarkusVertxVaadinService;
    }

    @ApplicationScoped
    @Unremovable
    public static class ContextualStorageManager extends AbstractContextualStorageManager<String> {

        public ContextualStorageManager() {
            super(true);
        }

        /**
         * Service destroy event observer.
         * <p>
         * During application shutdown it is container specific whether this
         * observer being called, or not. Application context destroy may happen
         * earlier, and cleanup done by {@link #destroyAll()}.
         *
         * @param event
         *            service destroy event
         */
        void onServiceDestroy(@Observes(notifyObserver = IF_EXISTS) ServiceDestroyEvent event) {
            if (!(event.getSource() instanceof VertxVaadinService)) {
                return;
            }
            VertxVaadinService service = (VertxVaadinService) event.getSource();
            String servletName = service.getServiceName();
            destroy(servletName);
        }
    }
}
