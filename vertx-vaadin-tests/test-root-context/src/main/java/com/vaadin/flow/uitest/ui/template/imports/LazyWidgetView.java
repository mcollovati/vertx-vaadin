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
package com.vaadin.flow.uitest.ui.template.imports;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.imports.LazyWidgetView", layout = ViewTestLayout.class)
@Tag("x-lazy-widget")
@JsModule("x-lazy-widget.js")
public class LazyWidgetView extends PolymerTemplate<LazyWidgetView.Model> {
    static final String GREETINGS_TEMPLATE = "Greetings from server, %s!";

    public LazyWidgetView() {
        setId("template");
    }

    public interface Model extends TemplateModel {
        void setHasGreeting(boolean hasGreeting);

        void setGreeting(String greeting);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getModel().setHasGreeting(false);
        getModel().setGreeting("");
    }

    @ClientCallable
    void greet(String name) {
        getModel().setGreeting(String.format(GREETINGS_TEMPLATE, name));
        getModel().setHasGreeting(name != null && !name.isEmpty());
    }
}
