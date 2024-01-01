/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;
import io.vertx.core.http.HttpServerRequest;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;

@Route(value = "com.vaadin.flow.uitest.ui.PageView", layout = ViewTestLayout.class)
public class PageView extends AbstractDivView {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        Input input = new Input();
        input.setId("input");
        input.clear();

        Div updateButton = new Div();
        updateButton.setId("button");
        updateButton.setText("Update page title");
        updateButton.addClickListener(e -> {
            getPage().setTitle(input.getValue());
        });

        Div overrideButton = new Div();
        overrideButton.setId("override");
        overrideButton.setText("Triggers two updates");
        overrideButton.addClickListener(e -> {
            getPage().setTitle(input.getValue());
            getPage().setTitle("OVERRIDDEN");
        });

        Div reloadButton = new Div();
        reloadButton.setId("reload");
        reloadButton.setText("Reloads the page");
        reloadButton.addClickListener(e -> {
            getPage().reload();
        });

        VertxVaadinRequest request = (VertxVaadinRequest) VaadinRequest.getCurrent();
        HttpServerRequest httpServletRequest = request.getRequest();
        String url = httpServletRequest.uri().replace(PageView.class.getName(), BaseHrefView.class.getName());

        Div setLocationButton = new Div();
        setLocationButton.setId("setLocation");
        setLocationButton.setText("Set page location");
        setLocationButton.addClickListener(e -> getPage().setLocation(url));

        Div openButton = new Div();
        openButton.setId("open");
        openButton.setText("Open url in a new tab");
        openButton.addClickListener(e -> getPage().open(url));

        add(input, updateButton, overrideButton, reloadButton, setLocationButton, openButton);
    }
}
