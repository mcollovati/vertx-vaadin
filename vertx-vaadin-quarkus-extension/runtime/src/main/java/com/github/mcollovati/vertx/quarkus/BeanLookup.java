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

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinServiceEnabled;

/**
 * Utility class for Quarkus CDI lookup, and instantiation.
 * <p>
 * Dependent beans are instantiated without any warning, but do not get
 * destroyed properly. {@link jakarta.annotation.PreDestroy} won't run.
 *
 * @param <T>
 *            Bean Type
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
class BeanLookup<T> {

    static final Annotation SERVICE = new ServiceLiteral();

    private final BeanManager beanManager;
    private final Class<T> type;
    private final Annotation[] qualifiers;
    private UnsatisfiedHandler unsatisfiedHandler = () -> {};
    private Consumer<AmbiguousResolutionException> ambiguousHandler = e -> {
        throw e;
    };

    private static final Annotation[] ANY = new Annotation[] {new AnyLiteral()};

    private static class ServiceLiteral extends AnnotationLiteral<VaadinServiceEnabled>
            implements VaadinServiceEnabled {}

    @FunctionalInterface
    public interface UnsatisfiedHandler {
        void handle();
    }

    BeanLookup(final BeanManager beanManager, final Class<T> type, final Annotation... qualifiers) {
        this.beanManager = beanManager;
        this.type = type;
        if (qualifiers.length > 0) {
            this.qualifiers = qualifiers;
        } else {
            this.qualifiers = ANY;
        }
    }

    BeanLookup<T> setUnsatisfiedHandler(final UnsatisfiedHandler unsatisfiedHandler) {
        this.unsatisfiedHandler = unsatisfiedHandler;
        return this;
    }

    BeanLookup<T> setAmbiguousHandler(final Consumer<AmbiguousResolutionException> ambiguousHandler) {
        this.ambiguousHandler = ambiguousHandler;
        return this;
    }

    T lookupOrElseGet(final Supplier<T> fallback) {
        final Set<Bean<?>> beans = this.beanManager.getBeans(this.type, this.qualifiers);
        if (beans == null || beans.isEmpty()) {
            this.unsatisfiedHandler.handle();
            return fallback.get();
        }
        final Bean<?> bean;
        try {
            bean = this.beanManager.resolve(beans);
        } catch (final AmbiguousResolutionException e) {
            this.ambiguousHandler.accept(e);
            return fallback.get();
        }
        final CreationalContext<?> ctx = this.beanManager.createCreationalContext(bean);
        // noinspection unchecked
        return (T) this.beanManager.getReference(bean, this.type, ctx);
    }

    T lookup() {
        return lookupOrElseGet(() -> null);
    }
}
