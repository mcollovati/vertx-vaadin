package com.github.mcollovati.vertx.vaadin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.github.mcollovati.vertx.support.StartupContext;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class DeploymentConfigurationFactory {

    public static DeploymentConfiguration createDeploymentConfiguration(Class<?> systemPropertyBaseClass, VaadinOptions vaadinOptions) {
        return new DefaultDeploymentConfiguration(systemPropertyBaseClass, vaadinOptions.asProperties());
    }
}
