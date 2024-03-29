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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.SynchronizedPropertyView", layout = ViewTestLayout.class)
public class SynchronizedPropertyView extends AbstractDivView {

    @Tag("input")
    public static class InputSync extends Component {
        public InputSync(Div label, String event) {
            getElement().setAttribute("placeholder", "Enter text here");
            label.setText("Server value on create: " + getElement().getProperty("value"));
            getElement().addPropertyChangeListener("value", event, e -> {});
            getElement().addEventListener(event, e -> {
                label.setText("Server value: " + getElement().getProperty("value"));
            });
        }

        public void setValue(String value) {
            getElement().setProperty("value", value);
        }
    }

    @Override
    protected void onShow() {
        valueAsNumberShimForIE11();
        addSimpleSync();
        addSyncWithInitialValue();
        addSyncOnKeyup();
        addSyncMultipleProperties();
    }

    private void addSimpleSync() {
        add(new Text("Synchronized on 'change' event"));
        Div label = new Div();
        label.setId("syncOnChangeLabel");
        InputSync syncOnChange = new InputSync(label, "change");
        syncOnChange.setId("syncOnChange");
        add(syncOnChange);
        add(label);
    }

    private void addSyncWithInitialValue() {
        add(new Text("Synchronized on 'change' event with initial value"));
        final Div syncOnChangeInitialValueLabel = new Div();
        syncOnChangeInitialValueLabel.setId("syncOnChangeInitialValueLabel");
        Element syncOnChangeInitialValue = ElementFactory.createInput();
        syncOnChangeInitialValueLabel.setText(
                "Server value on create: " + syncOnChangeInitialValue.getProperty("value"));
        syncOnChangeInitialValue.setAttribute("id", "syncOnChangeInitialValue");
        syncOnChangeInitialValue.addPropertyChangeListener("value", "change", event -> {});
        syncOnChangeInitialValue.addEventListener("change", e -> {
            syncOnChangeInitialValueLabel.setText(
                    "Server value in change listener: " + syncOnChangeInitialValue.getProperty("value"));
        });
        syncOnChangeInitialValue.setProperty("value", "initial");

        getElement().appendChild(syncOnChangeInitialValue);
        add(syncOnChangeInitialValueLabel);
    }

    private void addSyncOnKeyup() {
        Div label;
        add(new Text("Synchronized on 'keyup' event"));
        label = new Div();
        label.setId("syncOnKeyUpLabel");
        InputSync syncOnKeyUp = new InputSync(label, "keyup");
        syncOnKeyUp.setId("syncOnKeyUp");
        add(syncOnKeyUp);
        add(label);
    }

    private void addSyncMultipleProperties() {
        add(new Text("Synchronize 'value' on 'input' event and 'valueAsNumber' on 'blur'"));
        Div valueLabel = new Div();
        valueLabel.setId("multiSyncValueLabel");
        Div valueAsNumberLabel = new Div();
        valueAsNumberLabel.setId("multiSyncValueAsNumberLabel");

        Element multiSync = ElementFactory.createInput("number");
        multiSync.setAttribute("id", "multiSyncValue");
        multiSync.addPropertyChangeListener("valueAsNumber", "blur", event -> {});
        multiSync.addPropertyChangeListener("value", "input", event -> {});

        multiSync.addEventListener("input", e -> {
            valueLabel.setText("Server value: " + multiSync.getProperty("value"));
        });
        multiSync.addEventListener("blur", e -> {
            valueAsNumberLabel.setText("Server valueAsNumber: " + multiSync.getProperty("valueAsNumber"));
        });

        getElement().appendChild(multiSync);
        add(valueLabel, valueAsNumberLabel);
    }

    /**
     * Fixes the broken behavior of valueAsNumber property on IE11, used in
     * {@link #addSyncMultipleProperties()}
     */
    private void valueAsNumberShimForIE11() {
        getPage()
                .executeJs(
                        // @formatter:off
                        "var input = document.createElement('input');"
                                + "input.setAttribute('type', 'number');"
                                + "input.setAttribute('value', '123');"

                                // This is true only on IE11, and is fixed below:
                                + "if (input.value != input.valueAsNumber) {"
                                + "    Object.defineProperty(Object.getPrototypeOf(input), 'valueAsNumber', {"
                                + "        get: function () { return parseInt(this.value, 10); }"
                                + "    });"
                                + "}");
        // @formatter:on
    }
}
