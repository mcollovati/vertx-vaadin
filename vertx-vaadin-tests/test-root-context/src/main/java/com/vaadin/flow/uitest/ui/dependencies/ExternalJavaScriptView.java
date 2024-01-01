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
package com.vaadin.flow.uitest.ui.dependencies;

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@JavaScript(ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL)
@JavaScript(ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL_WITHOUT_PROTOCOL)
@Route(value = "com.vaadin.flow.uitest.ui.dependencies.ExternalJavaScriptView", layout = ViewTestLayout.class)
public class ExternalJavaScriptView extends Div {

    public ExternalJavaScriptView() {
        NativeButton button = new NativeButton("Add component", event -> {
            ComponentWithExternalJavaScript component = new ComponentWithExternalJavaScript();
            component.setId("componentWithExternalJavaScript");
            add(component);
        });
        button.setId("addComponentButton");
        add(button);
    }
}
