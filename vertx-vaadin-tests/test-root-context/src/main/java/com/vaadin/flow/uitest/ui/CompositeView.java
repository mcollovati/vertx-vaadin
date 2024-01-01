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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.webcomponent.PaperSlider;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.CompositeView", layout = ViewTestLayout.class)
public class CompositeView extends AbstractDivView {

    public static final String SERVER_INPUT_ID = "serverInput";
    public static final String SERVER_INPUT_BUTTON_ID = "serverInputButton";
    public static final String COMPOSITE_PAPER_SLIDER = "compositePaperSlider";

    public static class NameField extends Composite<Input> {

        @DomEvent("change")
        public static class NameChangeEvent extends ComponentEvent<NameField> {
            public NameChangeEvent(NameField source, boolean fromClient) {
                super(source, fromClient);
            }
        }

        private Input input = new Input();

        @Override
        protected Input initContent() {
            input.setPlaceholder("Enter your name");
            return input;
        }

        public void setName(String name) {
            input.setValue(name);
            fireEvent(new NameChangeEvent(this, false));
        }

        public String getName() {
            return input.getValue();
        }

        public void addNameChangeListener(ComponentEventListener<NameChangeEvent> nameChangeListener) {
            addListener(NameChangeEvent.class, nameChangeListener);
        }

        @Override
        public void setId(String id) {
            input.setId(id);
        }
    }

    public static class PaperSliderComposite extends Composite<PaperSlider> {}

    public CompositeView() {
        Div name = new Div();
        name.setText("Name on server: ");
        name.setId(CompositeNestedView.NAME_ID);

        NameField nameField = new NameField();
        nameField.setId(CompositeNestedView.NAME_FIELD_ID);
        nameField.addNameChangeListener(e -> {
            name.setText("Name on server: " + nameField.getName());
            String text = "Name value changed to " + nameField.getName() + " on the ";
            if (e.isFromClient()) {
                text += "client";
            } else {
                text += "server";
            }
            Div changeMessage = new Div();
            changeMessage.setText(text);
            add(changeMessage);
        });
        add(name, nameField, new Hr());

        Input serverInput = new Input();
        serverInput.setId(SERVER_INPUT_ID);
        NativeButton serverInputButton = createButton("Set", SERVER_INPUT_BUTTON_ID, e -> {
            nameField.setName(serverInput.getValue());
            serverInput.clear();
        });
        add(new Text("Enter a value to set the name on the server"), serverInput, serverInputButton);

        add(new Hr());

        PaperSliderComposite paperSliderComposite = new PaperSliderComposite();
        paperSliderComposite.setId(COMPOSITE_PAPER_SLIDER);
        add(paperSliderComposite);
    }
}
