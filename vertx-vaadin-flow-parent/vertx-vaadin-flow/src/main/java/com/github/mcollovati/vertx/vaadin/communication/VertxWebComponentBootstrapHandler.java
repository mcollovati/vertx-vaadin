package com.github.mcollovati.vertx.vaadin.communication;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.communication.WebComponentBootstrapHandler;

public class VertxWebComponentBootstrapHandler extends WebComponentBootstrapHandler {

    private static final String REQ_PARAM_URL = "url";
    private static final String PATH_PREFIX = "/web-component/web-component";

    /**
     * Returns the request's base url to use in constructing and initialising ui.
     *
     * @param request Request to the url for.
     * @return Request's url.
     */
    protected String getRequestUrl(VaadinRequest request) {
        return ((VertxVaadinRequest) request).getRequest().absoluteURI();
    }

    /**
     * Returns the service url needed for initialising the UI.
     *
     * @param request Request.
     * @return Service url for the given request.
     */
    protected String getServiceUrl(VaadinRequest request) {
        // get service url from 'url' parameter
        String url = request.getParameter(REQ_PARAM_URL);
        // if 'url' parameter was not available, use request url
        if (url == null) {
            url = getRequestUrl(request);
        }
        return url
            // +1 is to keep the trailing slash
            .substring(0, url.indexOf(PATH_PREFIX) + 1)
            // replace http:// or https:// with // to work with https:// proxies
            // which proxies to the same http:// url
            .replaceFirst("^" + ".*://", "//");
    }


}
