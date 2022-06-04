/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceEnabled;
import io.quarkus.arc.Unremovable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiator implementation based on Quarkus DI feature.
 * 
 * Quarkus DI solution (also called ArC) is based on the Contexts and Dependency
 * Injection for Java 2.0 specification, but it is not a full CDI
 * implementation. Only a subset of the CDI features is implemented.
 * 
 * See <a href="https://quarkus.io/guides/cdi-reference">Quarkus CDI
 * Reference</a> for further details.
 * 
 * @see Instantiator
 */
@VaadinServiceEnabled
@Unremovable
@ApplicationScoped
public class QuarkusInstantiator implements Instantiator {

    private static final String CANNOT_USE_CDI_BEANS_FOR_I18N = "Cannot use CDI beans for I18N, falling back to the default behavior.";
    private static final String FALLING_BACK_TO_DEFAULT_INSTANTIATION = "Falling back to default instantiation.";

    private AtomicBoolean i18NLoggingEnabled = new AtomicBoolean(true);
    private DefaultInstantiator delegate;

    @Inject
    BeanManager beanManager;

    /**
     * Gets the service class that this instantiator is supposed to work with.
     * 
     * @return the service class this instantiator is supposed to work with.
     */
    public Class<? extends VaadinService> getServiceClass() {
        return QuarkusVertxVaadinService.class;
    }

    /**
     * Gets the {@link BeanManager} instance.
     * 
     * @return the {@link BeanManager} instance.
     */
    public BeanManager getBeanManager() {
        return this.beanManager;
    }

    @Override
    public boolean init(final VaadinService service) {
        delegate = new DefaultInstantiator(service);
        return delegate.init(service)
                && getServiceClass().isAssignableFrom(service.getClass());
    }

    @Override
    public <T> T getOrCreate(Class<T> type) {
        return new BeanLookup<>(getBeanManager(), type)
                .setUnsatisfiedHandler(() -> getLogger().debug(
                        "'{}' is not a CDI bean. "
                                + FALLING_BACK_TO_DEFAULT_INSTANTIATION,
                        type.getName()))
                .setAmbiguousHandler(
                        e -> getLogger().debug(
                                "Multiple CDI beans found. "
                                        + FALLING_BACK_TO_DEFAULT_INSTANTIATION,
                                e))
                .lookupOrElseGet(() -> {
                    final T instance = delegate.getOrCreate(type);
                    // BeanProvider.injectFields(instance); // TODO maybe it
                    // could be fixed after Quarkus-Arc ticket
                    // https://github.com/quarkusio/quarkus/issues/2378 is done
                    return instance;
                });
    }

    @Override
    public I18NProvider getI18NProvider() {
        final BeanLookup<I18NProvider> lookup = new BeanLookup<>(
                getBeanManager(), I18NProvider.class, BeanLookup.SERVICE);
        if (i18NLoggingEnabled.compareAndSet(true, false)) {
            lookup.setUnsatisfiedHandler(() -> getLogger().info(
                    "Can't find any @VaadinServiceScoped bean implementing '{}'. "
                            + CANNOT_USE_CDI_BEANS_FOR_I18N,
                    I18NProvider.class.getSimpleName())).setAmbiguousHandler(
                            e -> getLogger().warn(
                                    "Found more beans for I18N. "
                                            + CANNOT_USE_CDI_BEANS_FOR_I18N,
                                    e));
        } else {
            lookup.setAmbiguousHandler(e -> {
            });
        }
        return lookup.lookupOrElseGet(delegate::getI18NProvider);
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(QuarkusInstantiator.class);
    }

    @Override
    public Stream<VaadinServiceInitListener> getServiceInitListeners() {
        return Stream.concat(delegate.getServiceInitListeners(),
                Stream.of(getBeanManager()::fireEvent));
    }

    @Override
    public <T extends Component> T createComponent(
            final Class<T> componentClass) {
        return this.delegate.createComponent(componentClass);
    }
}
