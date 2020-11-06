/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin;

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.polymertemplate.TemplateParser;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.internal.BrowserLiveReloadAccess;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.BootstrapListener;
import com.vaadin.flow.server.DependencyFilter;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * A {@link Instantiator} wrapper that overwrites some Vaadin implementations.
 */
class VertxVaadinInstantiator implements Instantiator {

    private final Instantiator delegate;

    VertxVaadinInstantiator(Instantiator delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean init(VaadinService service) {
        return delegate.init(service);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOrCreate(Class<T> type) {
        if (BrowserLiveReloadAccess.class.equals(type)) {
            return (T) new VertxVaadinBrowserLiveReload.Access();
        }
        return delegate.getOrCreate(type);
    }

    @Override
    public Stream<VaadinServiceInitListener> getServiceInitListeners() {
        return delegate.getServiceInitListeners();
    }

    @Override
    public <T extends Component> T createComponent(Class<T> componentClass) {
        return delegate.createComponent(componentClass);
    }

    @Override
    @Deprecated
    public Stream<BootstrapListener> getBootstrapListeners(Stream<BootstrapListener> serviceInitListeners) {
        return delegate.getBootstrapListeners(serviceInitListeners);
    }

    @Override
    public Stream<DependencyFilter> getDependencyFilters(Stream<DependencyFilter> serviceInitFilters) {
        return delegate.getDependencyFilters(serviceInitFilters);
    }

    @Override
    public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
        return delegate.createRouteTarget(routeTargetType, event);
    }

    public static Instantiator get(UI ui) {
        return Instantiator.get(ui);
    }

    @Override
    public I18NProvider getI18NProvider() {
        return delegate.getI18NProvider();
    }

    @Override
    public TemplateParser getTemplateParser() {
        return delegate.getTemplateParser();
    }


}

