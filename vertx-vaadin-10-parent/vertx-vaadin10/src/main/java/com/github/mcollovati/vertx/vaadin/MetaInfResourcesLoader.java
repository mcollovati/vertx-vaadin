package com.github.mcollovati.vertx.vaadin;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class MetaInfResourcesLoader implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService service = event.getSource();
        if (service instanceof VertxVaadinService) {
            new StubServletContext(((VertxVaadinService) service).getVertx())
                .getResourcePaths("/META-INF/resources/");
        }
    }
}
