package com.github.mcollovati.vertx.vaadin.communication;

import com.github.mcollovati.vertx.vaadin.VertxBootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.IndexHtmlRequestHandler;

public class VertxIndexHtmlRequestHandler extends IndexHtmlRequestHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return !VertxBootstrapHandler.isFrameworkInternalRequest(request) && request
                .getService().getBootstrapUrlPredicate().isValidUrl(request);
    }

}
