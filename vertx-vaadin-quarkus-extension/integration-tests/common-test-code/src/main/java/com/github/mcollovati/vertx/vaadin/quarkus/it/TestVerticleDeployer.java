package com.github.mcollovati.vertx.vaadin.quarkus.it;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

import com.github.mcollovati.vertx.quarkus.VaadinVerticleConfigurer;
import com.github.mcollovati.vertx.quarkus.VaadinVerticleDeployer;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static com.github.mcollovati.vertx.quarkus.VaadinVerticleDeployer.errorHandler;

@ApplicationScoped
public class TestVerticleDeployer {

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8888")
    int serverPort;

    public void init(@Observes StartupEvent e, Vertx vertx,
            TestVerticle verticle) {
        VaadinVerticleDeployer.startVerticle(vertx, verticle,
                vaadinVerticleConfigurer(), errorHandler(verticle));
    }

    VaadinVerticleConfigurer vaadinVerticleConfigurer() {
        return config -> {
            config.put("server", new JsonObject().put("port", serverPort));
        };
    }

}
