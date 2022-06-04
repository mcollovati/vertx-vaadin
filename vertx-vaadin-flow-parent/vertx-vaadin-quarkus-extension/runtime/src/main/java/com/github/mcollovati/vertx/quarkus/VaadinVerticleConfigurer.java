package com.github.mcollovati.vertx.quarkus;

import io.vertx.core.json.JsonObject;

public interface VaadinVerticleConfigurer {

    void apply(JsonObject config);

}
