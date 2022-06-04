package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.inject.spi.BeanManager;

import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.web.sstore.ExtendedSessionStore;
import io.vertx.core.Vertx;

public class QuarkusVertxVaadin extends VertxVaadin {

    private static final String BEANMANAGER_KEY = QuarkusVertxVaadin.class.getName() + "-" + BeanManager.class.getName();

    protected QuarkusVertxVaadin(Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext, BeanManager beanManager) {
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
