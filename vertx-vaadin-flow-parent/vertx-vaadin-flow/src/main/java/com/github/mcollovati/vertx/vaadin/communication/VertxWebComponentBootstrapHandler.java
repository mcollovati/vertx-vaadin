/*
 * The MIT License
 * Copyright © 2016-2019 Marco Collovati (mcollovati@gmail.com)
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
