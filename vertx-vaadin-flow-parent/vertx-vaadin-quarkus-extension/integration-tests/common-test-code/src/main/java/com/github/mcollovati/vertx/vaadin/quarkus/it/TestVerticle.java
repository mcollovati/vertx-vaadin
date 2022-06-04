package com.github.mcollovati.vertx.vaadin.quarkus.it;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.mcollovati.vertx.quarkus.QuarkusVaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;

@ApplicationScoped
public class TestVerticle extends QuarkusVaadinVerticle {

    @Inject
    Counter counter;

    @Override
    protected void serviceInitialized(VertxVaadinService service,
            Router router) {
        super.serviceInitialized(service, router);
        router.route().order(0).handler(event -> {
            HttpServerRequest request = event.request();
            if (request.getParam("resetCounts") != null) {
                counter.reset();
                event.response().end();
            } else {
                String key = request.getParam("getCount");
                if (key != null) {
                    event.response().end(Integer.toString(counter.get(key)));
                } else {
                    event.next();
                }
            }
        });
    }
}
