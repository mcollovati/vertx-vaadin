package com.github.mcollovati.vertx.vaadin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DeploymentConfigurationFactory {

    public static DeploymentConfiguration createDeploymentConfiguration(Class<?> systemPropertyBaseClass, JsonObject config) {
        return new DefaultDeploymentConfiguration(systemPropertyBaseClass, initProperties(config));
    }

    private static Properties initProperties(JsonObject config) {
        Properties initParameters = new Properties();
        initParameters.putAll((Map) adaptJson(config.getMap()));
        return initParameters;
    }

    private static Object adaptJson(Object object) {
        if (object instanceof Collection) {
            return ((Collection<?>) object).stream()
                .map(DeploymentConfigurationFactory::adaptJson)
                .collect(Collectors.toList());
        } else if (object instanceof Map) {
            LinkedHashMap<?, Object> map = new LinkedHashMap<>((Map<?, ?>) object);
            map.replaceAll((k, v) -> adaptJson(v));
            return map;
        } else if (object instanceof JsonObject) {
            return adaptJson(((JsonObject) object).getMap());
        } else if (object instanceof JsonArray) {
            return adaptJson(((JsonArray) object).getList());
        }
        return object;
    }

}
