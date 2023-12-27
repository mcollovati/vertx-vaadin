package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadin;

public abstract class QuarkusVaadinVerticle extends VaadinVerticle {

    @Inject
    BeanManager beanManager;

    @Override
    protected VertxVaadin createVertxVaadin(StartupContext startupContext) {
        return new QuarkusVertxVaadin(vertx, startupContext, beanManager);
    }
}
