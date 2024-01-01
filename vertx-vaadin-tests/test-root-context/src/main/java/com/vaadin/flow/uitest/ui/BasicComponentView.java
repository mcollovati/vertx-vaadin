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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.BasicComponentView", layout = ViewTestLayout.class)
public class BasicComponentView extends AbstractDivView {

    public static final String TEXT =
            "This is the basic component view text component with some tags: <b><html></body>";
    public static final String BUTTON_TEXT = "Click me";
    public static final String DIV_TEXT = "Hello world";

    @Override
    protected void onShow() {
        getElement().getStyle().set("margin", "1em");
        getElement().setAttribute("id", "root");

        Text text = new Text(TEXT);

        Input input = new Input();
        input.setPlaceholder("Synchronized on change event");

        NativeButton button = new NativeButton(BUTTON_TEXT, e -> {
            Div greeting = new Div();
            greeting.addClassName("thankYou");
            String buttonText = e.getSource().getElement().getText();

            greeting.setText("Thank you for clicking \"" + buttonText
                    + "\" at (" + e.getClientX() + "," + e.getClientY()
                    + ")! The field value is " + input.getValue());

            greeting.addClickListener(e2 -> remove(greeting));
            add(greeting);
        });

        Div helloWorld = new Div();
        helloWorld.setText(DIV_TEXT);
        helloWorld.addClassName("hello");
        helloWorld.setId("hello-world");
        helloWorld.addClickListener(e -> {
            helloWorld.setText("Stop touching me!");
            helloWorld.getElement().getClassList().clear();
        });
        Style s = helloWorld.getElement().getStyle();
        s.set("color", "red");
        s.set("fontWeight", "bold");

        add(text, helloWorld, button, input);
    }
}
