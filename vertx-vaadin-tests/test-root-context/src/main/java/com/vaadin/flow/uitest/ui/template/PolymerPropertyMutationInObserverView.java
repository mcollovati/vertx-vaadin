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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;

@Route("com.vaadin.flow.uitest.ui.template.PolymerPropertyMutationInObserverView")
public class PolymerPropertyMutationInObserverView extends AbstractDivView {

    @Tag("property-mutation-in-observer")
    @JsModule("PolymerPropertyMutationInObserver.js")
    public static class PolymerPropertyMutationInObserver extends PolymerTemplate<Message> {

        public void setText(String text) {
            getModel().setText(text);
        }

        private Div getValueDiv(String eventOldValue, String eventValue) {
            Div div = new Div();
            div.setText(String.format(
                    "Event old value: %s, event value: %s, current model value: %s",
                    eventOldValue, eventValue, getModel().getText()));
            div.addClassName("model-value");
            return div;
        }
    }

    public PolymerPropertyMutationInObserverView() {
        PolymerPropertyMutationInObserver template = new PolymerPropertyMutationInObserver();
        template.setId("template");
        template.getElement()
                .addPropertyChangeListener(
                        "text",
                        event -> add(template.getValueDiv((String) event.getOldValue(), (String) event.getValue())));
        template.setText("initially set value");
        add(template);
    }
}
