package com.vaadin.flow;

import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DevModeRule implements TestRule {

    private final String productionModeProperty;

    public DevModeRule() {
        this("vaadin.productionMode");
    }

    public DevModeRule(String productionModeProperty) {
        this.productionModeProperty = productionModeProperty;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        boolean ignoreOnDevMode = description.getAnnotation(DevModeOnly.class) != null
                || description.getTestClass().getAnnotation(DevModeOnly.class) != null;
        boolean isProductionMode = Boolean.getBoolean(productionModeProperty) ||
                (System.getProperties().contains(productionModeProperty) &&  System.getProperty(productionModeProperty) == null);
        if (isProductionMode && ignoreOnDevMode) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    throw new AssumptionViolatedException(description.getDisplayName() + " can run only on development mode");
                }
            };
        }
        return base;
    }
}
