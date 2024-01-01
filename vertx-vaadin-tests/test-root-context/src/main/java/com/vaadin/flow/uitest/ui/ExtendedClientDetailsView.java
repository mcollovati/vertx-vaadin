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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.ExtendedClientDetailsView", layout = ViewTestLayout.class)
public class ExtendedClientDetailsView extends AbstractDivView {

    @Override
    protected void onShow() {
        Div screenWidth = createDiv("sw");
        Div screenHeight = createDiv("sh");
        Div windowInnerWidth = createDiv("ww");
        Div windowInnerHeight = createDiv("wh");
        Div bodyElementWidth = createDiv("bw");
        Div bodyElementHeight = createDiv("bh");
        Div devicePixelRatio = createDiv("pr");
        Div touchDevice = createDiv("td");

        // the sizing values cannot be set with JS but pixel ratio and touch
        // support can be faked
        NativeButton setValuesButton = new NativeButton("Set test values", event -> {
            getUI().ifPresent(ui -> ui.getPage()
                    .executeJs("{" + "window.devicePixelRatio = 2.0;" + "navigator.msMaxTouchPoints = 1;" + "}"));
        });
        setValuesButton.setId("set-values");

        NativeButton fetchDetailsButton = new NativeButton("Fetch client details", event -> {
            getUI().ifPresent(ui -> ui.getPage().retrieveExtendedClientDetails(details -> {
                screenWidth.setText("" + details.getScreenWidth());
                screenHeight.setText("" + details.getScreenHeight());
                windowInnerWidth.setText("" + details.getWindowInnerWidth());
                windowInnerHeight.setText("" + details.getWindowInnerHeight());
                bodyElementWidth.setText("" + details.getBodyClientWidth());
                bodyElementHeight.setText("" + details.getBodyClientHeight());
                devicePixelRatio.setText("" + details.getDevicePixelRatio());
                touchDevice.setText("" + details.isTouchDevice());
            }));
        });
        fetchDetailsButton.setId("fetch-values");

        add(setValuesButton, fetchDetailsButton);
    }

    private Div createDiv(String id) {
        Div div = new Div();
        div.setId(id);
        add(div);
        return div;
    }
}
