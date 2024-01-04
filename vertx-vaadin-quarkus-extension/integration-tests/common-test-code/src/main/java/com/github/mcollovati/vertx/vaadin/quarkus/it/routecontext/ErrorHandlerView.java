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

import java.util.UUID;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLink;

import com.github.mcollovati.vertx.quarkus.annotation.NormalRouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;

@RouteScoped
@RouteScopeOwner(ErrorParentView.class)
@ParentLayout(ErrorParentView.class)
public class ErrorHandlerView extends AbstractCountedView implements HasErrorParameter<CustomException> {

    public static final String PARENT = "parent";

    @Inject
    @RouteScopeOwner(ErrorHandlerView.class)
    private Instance<ErrorBean1> bean1;

    @Inject
    @RouteScopeOwner(ErrorHandlerView.class)
    private Instance<ErrorBean2> bean2;

    private AbstractCountedBean current;

    @NormalRouteScoped
    @RouteScopeOwner(ErrorHandlerView.class)
    public static class ErrorBean1 extends AbstractCountedBean {

        @PostConstruct
        void init() {
            setData(UUID.randomUUID().toString());
        }
    }

    @NormalRouteScoped
    @RouteScopeOwner(ErrorHandlerView.class)
    public static class ErrorBean2 extends AbstractCountedBean {

        @PostConstruct
        void init() {
            setData(UUID.randomUUID().toString());
        }
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<CustomException> parameter) {
        add(new RouterLink(PARENT, ErrorParentView.class));

        Div div = new Div();
        div.setId("bean-data");

        NativeButton button = new NativeButton("switch content", ev -> {
            if (current instanceof ErrorBean1) {
                current = bean2.get();
            } else {
                current = bean1.get();
            }
            div.setText(current.getData());
        });
        button.setId("switch-content");
        add(button);
        current = bean1.get();
        div.setText(current.getData());
        add(div);

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
