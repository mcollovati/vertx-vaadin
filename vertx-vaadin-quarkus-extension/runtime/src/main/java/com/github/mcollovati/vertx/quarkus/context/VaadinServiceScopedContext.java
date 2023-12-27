/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.mcollovati.vertx.quarkus.context;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import java.lang.annotation.Annotation;

import com.github.mcollovati.vertx.quarkus.QuarkusVertxVaadinService;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceScoped;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinService;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Context for {@link VaadinServiceScoped @VaadinServiceScoped} beans.
 */
public class VaadinServiceScopedContext extends AbstractContext {

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual,
            boolean createIfNotExist) {
        String name = VertxVaadinService.getCurrent().getServiceName();
        return BeanProvider
                .getContextualReference(Arc.container().beanManager(),
                        ContextualStorageManager.class, false)
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
    public static class ContextualStorageManager
            extends AbstractContextualStorageManager<String> {

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
        void onServiceDestroy(
                @Observes(notifyObserver = IF_EXISTS) ServiceDestroyEvent event) {
            if (!(event.getSource() instanceof VertxVaadinService)) {
                return;
            }
            VertxVaadinService service = (VertxVaadinService) event.getSource();
            String servletName = service.getServiceName();
            destroy(servletName);
        }

    }

}
