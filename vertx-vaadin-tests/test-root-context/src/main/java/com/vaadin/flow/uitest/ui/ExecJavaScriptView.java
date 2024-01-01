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

import java.io.Serializable;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.internal.JsonUtils;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;
import elemental.json.Json;

@Route(value = "com.vaadin.flow.uitest.ui.ExecJavaScriptView", layout = ViewTestLayout.class)
public class ExecJavaScriptView extends AbstractDivView {
    @Override
    protected void onShow() {
        NativeButton alertButton = createJsButton("Alert", "alertButton", "window.alert($0)", "Hello world");
        NativeButton focusButton = createJsButton("Focus Alert button", "focusButton", "$0.focus()", alertButton);
        NativeButton swapText = createJsButton(
                "Swap button texts",
                "swapButton",
                "(function() {var t = $0.textContent; $0.textContent = $1.textContent; $1.textContent = t;})()",
                alertButton,
                focusButton);
        NativeButton logButton = createJsButton(
                "Log",
                "logButton",
                "console.log($0)",
                JsonUtils.createArray(Json.create("Hello world"), Json.create(true)));

        NativeButton createElementButton = createButton("Create and update element", "createButton", e -> {
            Input input = new Input();
            input.addClassName("newInput");
            input.getElement().executeJs("this.value=$0", "Value from js");
            add(input);
        });

        add(alertButton, focusButton, swapText, logButton, createElementButton);
    }

    private NativeButton createJsButton(String text, String id, String script, Serializable... arguments) {
        return createButton(text, id, e -> UI.getCurrent().getPage().executeJs(script, arguments));
    }
}
