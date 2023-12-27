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
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import java.util.Set;

import com.github.mcollovati.vertx.quarkus.QuarkusVertxVaadinService;
import com.github.mcollovati.vertx.quarkus.TestQuarkusVertxVaadinServletService;
import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinService;
import com.github.mcollovati.vertx.quarkus.context.VaadinServiceScopedContext.ContextualStorageManager;

public class ServiceUnderTestContext implements UnderTestContext {
    private QuarkusVertxVaadinService service;
    private static int NDX;
    private final BeanManager beanManager;

    public ServiceUnderTestContext(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public void activate() {
        NDX++;
        service = new TestQuarkusVertxVaadinServletService(beanManager, NDX + "");
        VaadinService.setCurrent(service);
    }

    @Override
    public void tearDownAll() {
        VaadinService.setCurrent(null);
        Context appContext = beanManager.getContext(ApplicationScoped.class);
        Set<Bean<?>> beans = beanManager
            .getBeans(ContextualStorageManager.class);
        ((AlterableContext) appContext).destroy(beanManager.resolve(beans));
    }

    @Override
    public void destroy() {
        if (service != null) {
            beanManager.fireEvent(new ServiceDestroyEvent(service));
        }
    }

    public QuarkusVertxVaadinService getService() {
        return service;
    }
}
