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

import com.vaadin.server.BootstrapHandler;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

public class VertxBootstrapHandler extends BootstrapHandler {
    @Override
    protected String getServiceUrl(BootstrapContext context) {
        String pathInfo = context.getRequest().getPathInfo();
        if (pathInfo == null) {
            return null;
        } else {
            /*
             * Make a relative URL to the servlet by adding one ../ for each
             * path segment in pathInfo (i.e. the part of the requested path
             * that comes after the servlet mapping)
             */
            return VaadinServletService.getCancelingRelativePath(pathInfo);
        }
    }

    @Override
    public String getThemeName(BootstrapContext context) {
        String themeName = context.getRequest()
            .getParameter(VaadinServlet.URL_PARAMETER_THEME);
        if (themeName == null) {
            themeName = super.getThemeName(context);
        }
        return themeName;
    }

    @Override
    protected String getContextRootPath(BootstrapContext context) {
        return VertxVaadinService
            .getContextRootRelativePath(context.getRequest()) + "/";
    }

}
