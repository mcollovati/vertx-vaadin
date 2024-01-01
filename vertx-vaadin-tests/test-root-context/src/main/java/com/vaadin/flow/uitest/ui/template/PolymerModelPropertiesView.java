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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.PolymerModelPropertiesView", layout = ViewTestLayout.class)
@Tag("model-properties")
@JsModule("PolymerModelProperties.js")
public class PolymerModelPropertiesView extends PolymerTemplate<Message> {

    @DomEvent("text-changed")
    public static class ValueChangeEvent extends ComponentEvent<PolymerModelPropertiesView> {
        public ValueChangeEvent(PolymerModelPropertiesView source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    public PolymerModelPropertiesView() {
        setId("template");
        getModel().setText("foo");

        getElement().addPropertyChangeListener("text", "text-changed", event -> {});

        addListener(ValueChangeEvent.class, event -> {
            getUI().get().add(addUpdateElement("property-update-event"));
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getUI().get().add(addUpdateElement("property-value"));
    }

    @EventHandler
    private void valueUpdated() {
        getUI().get().add(addUpdateElement("value-update"));
    }

    private Div addUpdateElement(String id) {
        Div div = new Div();
        div.setText("Property value:" + getElement().getProperty("text") + ", model value: "
                + getModel().getText());
        div.setId(id);
        return div;
    }
}
