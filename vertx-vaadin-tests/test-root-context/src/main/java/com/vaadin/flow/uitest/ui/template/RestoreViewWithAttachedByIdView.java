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
package com.vaadin.flow.uitest.ui.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.RestoreViewWithAttachedByIdView", layout = ViewTestLayout.class)
public class RestoreViewWithAttachedByIdView extends AbstractDivView {

    private TemplateWithInjectedId template;
    private Label label;

    private Component current;

    @Tag("template-with-injected-id")
    @JsModule("TemplateWithInjectedId.js")
    public static class TemplateWithInjectedId extends PolymerTemplate<TemplateModel> {

        @Id("ignore")
        private Div ignore;

        @Id("target")
        private Div target;

        public TemplateWithInjectedId() {
            target.setText("Server Side Text");
        }
    }

    public RestoreViewWithAttachedByIdView() {
        template = new TemplateWithInjectedId();
        label = new Label("Switched component");
        label.setId("info");
        template.setId("template");

        add(template);

        NativeButton button = new NativeButton("Switch components");
        current = template;
        button.addClickListener(event -> {
            remove(current);
            if (current == template) {
                current = label;
            } else {
                current = template;
            }
            add(current);
        });
        add(button);
    }
}
