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

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.JavaScriptReturnValueView", layout = ViewTestLayout.class)
public class JavaScriptReturnValueView extends AbstractDivView {

    public JavaScriptReturnValueView() {
        Div statusLabel = new Div();
        statusLabel.setId("status");

        // Callback to run an expression
        NativeRadioButtonGroup<SerializableFunction<String, PendingJavaScriptResult>> methodSelect =
                new NativeRadioButtonGroup<>("Method to use");
        methodSelect
                .addOption("Page.executeJavaScript", UI.getCurrent().getPage()::executeJs)
                .setId("execPage");
        methodSelect
                .addOption("Element.executeJavaScript", script -> getElement().executeJs(script))
                .setId("execElement");
        methodSelect
                .addOption("Element.callFunction", script -> {
                    getElement().executeJs("this.scriptToRun = new Function($0)", script);
                    return getElement().callJsFunction("scriptToRun");
                })
                .setId("callElement");

        // Value expression
        NativeRadioButtonGroup<String> valueSelect = new NativeRadioButtonGroup<>("Value type");
        valueSelect.addOption("String", "'foo'");
        valueSelect.addOption("Number", "42");
        valueSelect.addOption("null", "null");
        valueSelect.addOption("Error", "new Error('message')");

        // Promise semantics to use
        NativeRadioButtonGroup<String> resolveRejectSelect = new NativeRadioButtonGroup<>("Outcome");
        resolveRejectSelect.addOption("Success", "resolve");
        resolveRejectSelect.addOption("Failure", "reject");

        // Builds JS expression from value expression and promise semantics
        NativeRadioButtonGroup<SerializableBiFunction<String, String, String>> executionSelect =
                new NativeRadioButtonGroup<>("Execution type");
        executionSelect.addOption("Synchronous", (value, resolveOrReject) -> {
            if ("resolve".equals(resolveOrReject)) {
                return "return " + value;
            } else {
                return "throw " + value;
            }
        });
        executionSelect.addOption(
                "Resolved promise",
                (value, resolveOrReject) -> "return Promise." + resolveOrReject + "(" + value + ")");
        executionSelect.addOption(
                "Timeout",
                (value, resolveOrReject) -> "return new Promise((resolve, reject) => {setTimeout(() => "
                        + resolveOrReject + "(" + value + "), 1000)})");

        NativeButton runButton = createButton("Run", "run", event -> {
            statusLabel.setText("Running...");

            String scriptToRun = executionSelect.selected.apply(valueSelect.selected, resolveRejectSelect.selected);

            PendingJavaScriptResult result = methodSelect.selected.apply(scriptToRun);

            result.then(String.class, statusLabel::setText, error -> statusLabel.setText("Error: " + error));
        });

        NativeButton clearButton = createButton("Clear", "clear", event -> {
            statusLabel.setText("");
        });

        add(methodSelect, valueSelect, resolveRejectSelect, executionSelect, runButton, clearButton, statusLabel);
    }

    private static class NativeRadioButtonGroup<T> extends Composite<Div> {
        private final String group;
        private T selected;

        public NativeRadioButtonGroup(String caption) {
            getContent().add(new Text(caption));
            this.group = caption.replaceAll(" ", "").toLowerCase();
        }

        public Input addOption(String caption, T value) {
            Input input = new Input();
            input.setId(caption.replaceAll("[ .]", "").toLowerCase());

            input.getElement().setAttribute("name", group);
            input.getElement().setAttribute("type", "radio");

            // Last one to receive a change event is selected
            input.getElement()
                    .addEventListener("change", event -> selected = value)
                    .setFilter("element.checked");

            // Preselect first option
            if (selected == null) {
                assert value != null;

                selected = value;
                input.getElement().setAttribute("checked", true);
            }

            Label label = new Label(caption);
            label.setFor(input);

            getContent().add(new Div(input, label));

            return input;
        }
    }
}
