/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus;

import jakarta.enterprise.inject.spi.BeanManager;

import io.vertx.core.Vertx;

import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.web.sstore.ExtendedSessionStore;

public class QuarkusVertxVaadin extends VertxVaadin {

    private static final String BEANMANAGER_KEY =
            QuarkusVertxVaadin.class.getName() + "-" + BeanManager.class.getName();

    protected QuarkusVertxVaadin(
            Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext, BeanManager beanManager) {
        super(vertx, sessionStore, injectBeanManager(startupContext, beanManager));
    }

    protected QuarkusVertxVaadin(Vertx vertx, StartupContext startupContext, BeanManager beanManager) {
        super(vertx, injectBeanManager(startupContext, beanManager));
    }

    @Override
    protected VertxVaadinService createVaadinService() {
        BeanManager beanManager = computeWithStartupContext(ctx -> {
            BeanManager obj = (BeanManager) ctx.servletContext().getAttribute(BEANMANAGER_KEY);
            ctx.servletContext().removeAttribute(BEANMANAGER_KEY);
            return obj;
        });
        return new QuarkusVertxVaadinService(this, createDeploymentConfiguration(), beanManager);
    }

    protected static StartupContext injectBeanManager(StartupContext startupContext, BeanManager beanManager) {
        startupContext.servletContext().setAttribute(BEANMANAGER_KEY, beanManager);
        return startupContext;
    }
}
