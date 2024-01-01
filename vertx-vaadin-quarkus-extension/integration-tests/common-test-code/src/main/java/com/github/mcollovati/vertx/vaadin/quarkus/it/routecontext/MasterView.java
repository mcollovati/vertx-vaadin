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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;

@RouteScoped
@Route("")
@RoutePrefix("master")
public class MasterView extends AbstractCountedView implements RouterLayout, AfterNavigationObserver {

    public static final String ASSIGNED = "assigned";
    public static final String ASSIGNED_BEAN_LABEL = "ASSIGNED";
    public static final String APART = "apart";
    public static final String APART_BEAN_LABEL = "APART";

    @Inject
    @RouteScopeOwner(MasterView.class)
    AssignedBean assignedBean;

    private Label assignedLabel;

    @PostConstruct
    private void init() {
        assignedLabel = new Label();
        assignedLabel.setId(ASSIGNED_BEAN_LABEL);
        add(
                new Label("MASTER"),
                new Div(assignedLabel),
                new Div(new RouterLink(ASSIGNED, DetailAssignedView.class)),
                new Div(new RouterLink(APART, DetailApartView.class)));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        assignedLabel.setText(assignedBean.getData());
    }
}
