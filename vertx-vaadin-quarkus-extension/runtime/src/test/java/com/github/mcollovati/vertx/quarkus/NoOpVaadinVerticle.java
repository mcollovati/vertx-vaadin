package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.context.Dependent;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import io.quarkus.arc.DefaultBean;
import io.vertx.core.Promise;

@Dependent
@DefaultBean
public class NoOpVaadinVerticle extends VaadinVerticle {

    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        // Do Nothing
    }

    @Override
    public void stop(Promise<Void> stopFuture) {
        // Do nothing
    }
}
