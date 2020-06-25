package com.github.mcollovati.vertx.vaadin.communication;

import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.communication.JavaScriptBootstrapHandler;
import com.vaadin.flow.server.communication.StreamRequestHandler;
import com.vaadin.flow.server.communication.UidlRequestHandler;
import com.vaadin.flow.server.communication.WebComponentBootstrapHandler;

public class RequestHandlerReplacements {

    public static RequestHandler replace(RequestHandler requestHandler) {
        if (requestHandler instanceof StreamRequestHandler) {
            return new VertxStreamRequestHandler();
        } else if (requestHandler instanceof WebComponentBootstrapHandler) {
            return new VertxWebComponentBootstrapHandler();
        } else if (requestHandler instanceof JavaScriptBootstrapHandler) {
            return new VertxJavaScriptBootstrapHandler();
        } else if (requestHandler instanceof UidlRequestHandler) {
            return new VertxUidlRequestHandler();
        }
        return requestHandler;
    }
}
