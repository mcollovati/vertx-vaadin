package com.github.mcollovati.failures.lookup;

import java.util.Collection;
import java.util.Map;

import com.vaadin.flow.di.AbstractLookupInitializer;
import com.vaadin.flow.function.VaadinApplicationInitializationBootstrap;
import com.vaadin.flow.server.VaadinContext;

// Added to classpath just to test initialization failure
public class FakeLookupInitializer implements AbstractLookupInitializer {

    @Override
    public void initialize(VaadinContext context, Map<Class<?>, Collection<Class<?>>> services, VaadinApplicationInitializationBootstrap bootstrap) {
        // do nothing
    }
}
