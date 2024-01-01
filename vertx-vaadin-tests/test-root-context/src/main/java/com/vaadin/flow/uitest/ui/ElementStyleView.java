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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.ElementStyleView", layout = ViewTestLayout.class)
public class ElementStyleView extends AbstractDivView {

    static final String GREEN_BORDER = "4px solid rgb(0, 255, 0)";
    static final String RED_BORDER = "10px solid rgb(255, 0, 0)";

    @Override
    protected void onShow() {
        Element mainElement = getElement();
        mainElement.getStyle().set("--foo", RED_BORDER);

        Div div = new Div();
        div.setId("red-border");
        div.getElement().getStyle().set("border", "var(--foo)");
        div.setText("Div");

        Div div2 = new Div();
        div2.setId("green-border");
        div2.setText("Div 2");
        div2.getStyle().set("--foo", GREEN_BORDER);
        div2.getElement().getStyle().set("border", "var(--foo)");
        add(div, div2);
    }
}
