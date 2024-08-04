package com.github.mcollovati.vertx.vaadin;

import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.NavigationEvent;
import com.vaadin.flow.server.DependencyFilter;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.MenuAccessControl;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;

public class VertxVaadinInstantiator implements Instantiator {

    private static Class<?> copilotInitListener;
    static {
        try{
            copilotInitListener = VertxVaadinInstantiator.class.getClassLoader().loadClass("com.vaadin.copilot.CopilotIndexHtmlLoader");
        } catch (Exception ex) {
            LoggerFactory.getLogger(VertxVaadinInstantiator.class).debug("Copilot not present");
        }
    }

    @Override
    public Stream<VaadinServiceInitListener> getServiceInitListeners() {
        return delegate.getServiceInitListeners()
                // Disable Copilot because of hard-coded Servlet API usage
                .filter(listener -> copilotInitListener == null || !copilotInitListener.isInstance(listener.getClass()));
    }

    @Override
    public Stream<IndexHtmlRequestListener> getIndexHtmlRequestListeners(Stream<IndexHtmlRequestListener> indexHtmlRequestListeners) {
        return delegate.getIndexHtmlRequestListeners(indexHtmlRequestListeners);
    }

    @Override
    public Stream<DependencyFilter> getDependencyFilters(Stream<DependencyFilter> serviceInitFilters) {
        return delegate.getDependencyFilters(serviceInitFilters);
    }

    @Override
    public <T> T getOrCreate(Class<T> type) {
        return delegate.getOrCreate(type);
    }

    @Override
    public Class<?> getApplicationClass(Object instance) {
        return delegate.getApplicationClass(instance);
    }

    @Override
    public Class<?> getApplicationClass(Class<?> clazz) {
        return delegate.getApplicationClass(clazz);
    }

    @Override
    public <T extends HasElement> T createRouteTarget(Class<T> routeTargetType, NavigationEvent event) {
        return delegate.createRouteTarget(routeTargetType, event);
    }

    @Override
    public <T extends Component> T createComponent(Class<T> componentClass) {
        return delegate.createComponent(componentClass);
    }

    public static Instantiator get(UI ui) {
        return Instantiator.get(ui);
    }

    @Override
    public I18NProvider getI18NProvider() {
        return delegate.getI18NProvider();
    }

    @Override
    public MenuAccessControl getMenuAccessControl() {
        return delegate.getMenuAccessControl();
    }

    private final Instantiator delegate;

    public VertxVaadinInstantiator(Instantiator delegate) {
        this.delegate = delegate;
    }
}
