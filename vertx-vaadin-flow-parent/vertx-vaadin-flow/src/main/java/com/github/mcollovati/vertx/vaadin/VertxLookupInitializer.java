package com.github.mcollovati.vertx.vaadin;

import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Map;

import com.vaadin.flow.di.LookupInitializer;
import com.vaadin.flow.function.VaadinApplicationInitializationBootstrap;
import com.vaadin.flow.internal.BrowserLiveReloadAccessor;
import com.vaadin.flow.server.VaadinContext;

import static java.util.Collections.singletonList;

public class VertxLookupInitializer extends LookupInitializer {
    @Override
    public void initialize(VaadinContext context, Map<Class<?>, Collection<Class<?>>> services,
                           VaadinApplicationInitializationBootstrap bootstrap) throws ServletException {
        services.put(BrowserLiveReloadAccessor.class, singletonList(VertxVaadinBrowserLiveReload.Accessor.class));
        super.initialize(context, services, bootstrap);

    }
}
