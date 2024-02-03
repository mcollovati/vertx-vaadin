/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.quarkus;

import jakarta.enterprise.inject.spi.BeanManager;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instantiator implementation for Quarkus.
 *
 * New instances are created by default by QuarkusInstantiatorFactory.
 *
 * @see InstantiatorFactory
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
public class QuarkusInstantiator implements Instantiator {

    private static final String CANNOT_USE_CDI_BEANS_FOR_I18N =
            "Cannot use CDI beans for I18N, falling back to the default behavior.";
    private static final String FALLING_BACK_TO_DEFAULT_INSTANTIATION = "Falling back to default instantiation.";

    private final AtomicBoolean i18NLoggingEnabled = new AtomicBoolean(true);
    private final DefaultInstantiator delegate;

    private final BeanManager beanManager;

    public QuarkusInstantiator(DefaultInstantiator delegate, BeanManager beanManager) {
        this.delegate = delegate;
        this.beanManager = beanManager;
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
    public <T> T getOrCreate(Class<T> type) {
        return new BeanLookup<>(getBeanManager(), type)
                .setUnsatisfiedHandler(() -> getLogger()
                        .debug("'{}' is not a CDI bean. " + FALLING_BACK_TO_DEFAULT_INSTANTIATION, type.getName()))
                .setAmbiguousHandler(
                        e -> getLogger().debug("Multiple CDI beans found. " + FALLING_BACK_TO_DEFAULT_INSTANTIATION, e))
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
        final BeanLookup<I18NProvider> lookup =
                new BeanLookup<>(getBeanManager(), I18NProvider.class, BeanLookup.SERVICE);
        if (i18NLoggingEnabled.compareAndSet(true, false)) {
            lookup.setUnsatisfiedHandler(() -> getLogger()
                            .info(
                                    "Can't find any @VaadinServiceScoped bean implementing '{}'. "
                                            + CANNOT_USE_CDI_BEANS_FOR_I18N,
                                    I18NProvider.class.getSimpleName()))
                    .setAmbiguousHandler(
                            e -> getLogger().warn("Found more beans for I18N. " + CANNOT_USE_CDI_BEANS_FOR_I18N, e));
        } else {
            lookup.setAmbiguousHandler(e -> {});
        }
        return lookup.lookupOrElseGet(delegate::getI18NProvider);
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(QuarkusInstantiator.class);
    }

    @Override
    public Stream<VaadinServiceInitListener> getServiceInitListeners() {
        return Stream.concat(
                delegate.getServiceInitListeners(), Stream.of(getBeanManager().getEvent()::fire));
    }

    @Override
    public <T extends Component> T createComponent(final Class<T> componentClass) {
        return this.delegate.createComponent(componentClass);
    }
}
