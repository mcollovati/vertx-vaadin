package com.github.mcollovati.vertx.vaadin.communication;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;

public class VertxJavaScriptBootstrapHandler extends JavaScriptBootstrapHandler {

    @Override
    protected String getRequestUrl(VaadinRequest request) {
        return ((VertxVaadinRequest)request).getRoutingContext().request().absoluteURI();
    }
}
