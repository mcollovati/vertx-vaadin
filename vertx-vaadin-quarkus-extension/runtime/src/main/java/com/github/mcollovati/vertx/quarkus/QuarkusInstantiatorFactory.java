package com.github.mcollovati.vertx.quarkus;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.server.VaadinService;
import io.quarkus.arc.Unremovable;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceEnabled;

/**
 * Instantiator factory implementation based on Quarkus DI feature.
 *
 * Quarkus DI solution (also called ArC) is based on the Contexts and Dependency
 * Injection for Java 2.0 specification, but it is not a full CDI
 * implementation. Only a subset of the CDI features is implemented.
 *
 * See <a href="https://quarkus.io/guides/cdi-reference">Quarkus CDI
 * Reference</a> for further details.
 *
 * @see InstantiatorFactory
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
@VaadinServiceEnabled
@Unremovable
@ApplicationScoped
public class QuarkusInstantiatorFactory implements InstantiatorFactory {

    @Inject
    BeanManager beanManager;

    @Override
    public Instantiator createInstantitor(VaadinService vaadinService) {
        if (!getServiceClass().isAssignableFrom(vaadinService.getClass())) {
            return null;
        }
        return new QuarkusInstantiator(new DefaultInstantiator(vaadinService), beanManager);
    }

    /**
     * Gets the service class that this instantiator factory is supposed to work
     * with.
     *
     * @return the service class this instantiator factory is supposed to work
     *         with.
     */
    public Class<? extends VaadinService> getServiceClass() {
        return QuarkusVertxVaadinService.class;
    }
}
