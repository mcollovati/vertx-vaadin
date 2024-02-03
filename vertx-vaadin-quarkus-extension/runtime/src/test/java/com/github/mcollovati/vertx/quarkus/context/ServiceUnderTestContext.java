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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.Set;

import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.VaadinService;

import com.github.mcollovati.vertx.quarkus.QuarkusVertxVaadinService;
import com.github.mcollovati.vertx.quarkus.TestQuarkusVertxVaadinServletService;
import com.github.mcollovati.vertx.quarkus.context.VaadinServiceScopedContext.ContextualStorageManager;

/*
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */

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
        Set<Bean<?>> beans = beanManager.getBeans(ContextualStorageManager.class);
        ((AlterableContext) appContext).destroy(beanManager.resolve(beans));
    }

    @Override
    public void destroy() {
        if (service != null) {
            beanManager.getEvent().fire(new ServiceDestroyEvent(service));
        }
    }

    public QuarkusVertxVaadinService getService() {
        return service;
    }
}
