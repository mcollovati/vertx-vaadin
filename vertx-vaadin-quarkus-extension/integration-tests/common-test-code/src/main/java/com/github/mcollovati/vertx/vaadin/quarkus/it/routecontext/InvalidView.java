/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.quarkus.it.routecontext;

import jakarta.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;

@Route("invalid-injection")
public class InvalidView extends Div {

    @Inject
    @RouteScopeOwner(ErrorParentView.class)
    /*
     * There is no a ErrorParentView in navigation: this injection has no scope.
     * Pseudo-scope @RouteScoped is used here with the component to immediately
     * get an exception, for normal scope proxy is created and the exception
     * won't be thrown immediately.
     */
    private ErrorParentView bean;

    public InvalidView() {
        setId("invalid-injection");
        setText("This view should not be shown since the "
                + "injection has no scope and an exception should be thrown");
    }
}
