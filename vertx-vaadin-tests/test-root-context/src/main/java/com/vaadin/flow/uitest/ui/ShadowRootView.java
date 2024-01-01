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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.dom.ShadowRoot;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.ShadowRootView", layout = ViewTestLayout.class)
public class ShadowRootView extends AbstractDivView implements HasDynamicTitle {

    @Override
    protected void onShow() {
        Div div = new Div();
        div.getElement().setAttribute("id", "test-element");
        add(div);

        ShadowRoot shadowRoot = div.getElement().attachShadow();
        Element shadowDiv = ElementFactory.createDiv();
        shadowDiv.setText("Div inside shadow DOM");
        shadowDiv.setAttribute("id", "shadow-div");
        shadowRoot.appendChild(shadowDiv);
        Element shadowLabel = ElementFactory.createLabel("Label inside shadow DOM");
        shadowLabel.setAttribute("id", "shadow-label");
        shadowRoot.appendChild(shadowLabel);

        NativeButton removeChild = createButton(
                "Remove the last child from the shadow root", "remove", event -> shadowRoot.removeChild(shadowLabel));
        add(removeChild);
    }

    @Override
    public String getPageTitle() {
        return "Shadow root view";
    }
}
