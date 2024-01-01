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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.EnabledView", layout = ViewTestLayout.class)
public class EnabledView extends AbstractDivView {

    public EnabledView() {
        setId("main");

        Div div = new Div();
        div.setText("Target to enable/disable");
        div.setId("enabled");
        div.getElement().setEnabled(false);

        Label label = new Label("Nested element");
        label.setId("nested-label");
        div.add(label);

        NativeButton updateStyle = createButton("Update target element property", "updateProperty", event -> {
            div.setClassName("foo");
            label.setClassName("bar");
        });
        updateStyle.getElement().setEnabled(false);

        NativeButton updateEnableButton = createButton("Change enable state for buttons", "enableButton", event -> {
            updateStyle.getElement().setEnabled(!updateStyle.getElement().isEnabled());
            updateStyle.setClassName("disabled", !updateStyle.getElement().isEnabled());
        });

        add(div, updateStyle, updateEnableButton);
    }
}
