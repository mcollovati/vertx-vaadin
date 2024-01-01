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

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.RerouteView", layout = ViewTestLayout.class)
public class RerouteView extends AbstractDivView {

    boolean reroute = false;

    public RerouteView() {
        NativeButton button = new NativeButton("Navigate to here");
        button.setId("navigate");
        button.addClickListener(e -> {
            button.getUI().ifPresent(ui -> ui.navigate("com.vaadin.flow.uitest.ui.RerouteView"));
        });

        CheckBox checkbox = new CheckBox("RerouteToError");
        checkbox.setId("check");
        checkbox.addValueChangeListener(event -> {
            reroute = checkbox.isChecked();
        });
        add(button);
        add(checkbox);
    }

    private void setReroute(boolean reroute) {
        this.reroute = reroute;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (reroute) {
            event.rerouteToError(NotFoundException.class, "Rerouting to error view");
        }

        super.beforeEnter(event);
    }

    @Tag(Tag.DIV)
    public class CheckBox extends HtmlContainer {

        Input input;
        Label captionLabel;

        public CheckBox() {
            input = new Input();
            input.getElement().setAttribute("type", "checkbox");
            input.getElement().addPropertyChangeListener("checked", "change", event -> {});
            add(input);
        }

        public CheckBox(String caption) {
            this();
            captionLabel = new Label(caption);
            add(captionLabel);
        }

        public Registration addValueChangeListener(ValueChangeListener<ValueChangeEvent<String>> listener) {
            return input.addValueChangeListener(listener);
        }

        public boolean isChecked() {
            return Boolean.parseBoolean(input.getElement().getProperty("checked"));
        }
    }
}
