package com.github.mcollovati.vertx.vaadin;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;

public class DeploymentConfigurationFactory {

    public static DeploymentConfiguration createDeploymentConfiguration(Class<?> systemPropertyBaseClass, VaadinOptions vaadinOptions) {
        return new DefaultDeploymentConfiguration(systemPropertyBaseClass, vaadinOptions.asProperties());
    }
}
