/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui.scroll;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.scroll.CustomScrollCallbacksView", layout = ViewTestLayout.class)
public class CustomScrollCallbacksView extends AbstractDivView implements HasUrlParameter<String> {
    private final Div viewName = new Div();
    private final Div log = new Div();

    public CustomScrollCallbacksView() {
        viewName.setId("view");

        log.setId("log");
        log.getStyle().set("white-space", "pre");

        UI.getCurrent()
                .getPage()
                .executeJs(
                        "window.Vaadin.Flow.setScrollPosition = function(xAndY) { $0.textContent += JSON.stringify(xAndY) + '\\n' }",
                        log);
        UI.getCurrent()
                .getPage()
                .executeJs("window.Vaadin.Flow.getScrollPosition = function() { return [42, -window.pageYOffset] }");

        RouterLink navigate = new RouterLink("Navigate", CustomScrollCallbacksView.class, "navigated");
        navigate.setId("navigate");

        Anchor back = new Anchor("javascript:history.go(-1)", "Back");
        back.setId("back");

        add(
                viewName,
                log,
                new Span("Scroll down to see navigation actions"),
                ScrollView.createSpacerDiv(2000),
                navigate,
                back);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        viewName.setText("Current view: " + parameter);
    }
}
