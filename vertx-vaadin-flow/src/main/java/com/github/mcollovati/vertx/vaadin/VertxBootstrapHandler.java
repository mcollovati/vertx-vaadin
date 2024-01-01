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

import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.shared.ApplicationConstants;

/**
 * Replacement of Vaadin BootstrapHandler to get rid of some explicit casts
 * in BootstrapUtils.
 */
public class VertxBootstrapHandler extends BootstrapHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        if (isFrameworkInternalRequest(request)) {
            // Never accidentally send a bootstrap page for what is considered
            // an internal request
            return false;
        }
        return super.canHandleRequest(request);
    }

    // Copy-Paste from BootstrapHandler and adapted to get rid of servlet stuff
    public static boolean isFrameworkInternalRequest(VaadinRequest request) {
        return isInternalRequestInsideServlet(
                request.getPathInfo(), request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER));
    }

    // Copy-Paste from HandlerHelper#isFrameworkInternalRequest(String, HttpServletRequest)},
    // since it is package protected
    static boolean isInternalRequestInsideServlet(
            String requestedPathWithoutServletMapping, String requestTypeParameter) {
        if (requestedPathWithoutServletMapping == null
                || requestedPathWithoutServletMapping.isEmpty()
                || "/".equals(requestedPathWithoutServletMapping)) {
            return requestTypeParameter != null;
        }
        return false;
    }
}
