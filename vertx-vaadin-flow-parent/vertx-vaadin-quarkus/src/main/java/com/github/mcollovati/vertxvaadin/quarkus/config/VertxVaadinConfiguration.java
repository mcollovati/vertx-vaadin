package com.github.mcollovati.vertxvaadin.quarkus.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "vertx-vaadin", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class VertxVaadinConfiguration {

    /**
     * Enables or disabled debug
     */
    @ConfigItem(defaultValue = "false")
    public boolean debug;

    /**
     * Server port
     */
    @ConfigItem(defaultValue = "8080")
    public int httpPort;

    /**
     * Vaadin configuration
     */
    public VaadinConfig vaadin;


    @Override
    public String toString() {
        return "VertxVaadinConfiguration{" +
            "debug=" + debug +
            ", httpPort=" + httpPort +
            ", vaadin=" + vaadin +
            '}';
    }

}
