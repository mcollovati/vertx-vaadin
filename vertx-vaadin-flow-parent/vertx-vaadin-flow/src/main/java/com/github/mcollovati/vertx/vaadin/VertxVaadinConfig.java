package com.github.mcollovati.vertx.vaadin;

import java.util.Collections;
import java.util.Enumeration;

import com.vaadin.flow.server.VaadinConfig;
import com.vaadin.flow.server.VaadinContext;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class VertxVaadinConfig implements VaadinConfig {

    private final JsonObject config;
    private final VaadinContext vaadinContext;

    public VertxVaadinConfig(JsonObject config, VaadinContext vaadinContext) {
        this.config = config;
        this.vaadinContext = vaadinContext;
    }

    @Override
    public VaadinContext getVaadinContext() {
        return vaadinContext;
    }

    @Override
    public Enumeration<String> getConfigParameterNames() {
        return Collections.enumeration(config.fieldNames());
    }

    @Override
    public String getConfigParameter(String name) {
        Object value = config.getValue(name);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return Json.encode(value);
        }
        return null;
    }

}
