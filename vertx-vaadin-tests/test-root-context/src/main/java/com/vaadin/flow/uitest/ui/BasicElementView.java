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

import java.util.Set;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;
import elemental.json.JsonObject;

@Route(value = "com.vaadin.flow.uitest.ui.BasicElementView", layout = ViewTestLayout.class)
public class BasicElementView extends AbstractDivView {

    private Registration helloWorldEventRemover;

    @Override
    protected void onShow() {
        Element mainElement = getElement();
        mainElement.getStyle().set("margin", "1em");

        Element button = ElementFactory.createButton("Click me");

        Element input = ElementFactory.createInput().setAttribute("placeholder", "Synchronized on change event");
        input.addPropertyChangeListener("value", "change", event -> {});

        button.addEventListener("click", e -> {
                    JsonObject eventData = e.getEventData();
                    String buttonText = eventData.getString("element.textContent");
                    int clientX = (int) eventData.getNumber("event.clientX");
                    int clientY = (int) eventData.getNumber("event.clientY");
                    Element greeting = ElementFactory.createDiv("Thank you for clicking \"" + buttonText + "\" at ("
                            + clientX + "," + clientY + ")! The field value is "
                            + input.getProperty("value"));
                    greeting.setAttribute("class", "thankYou");
                    greeting.addEventListener("click", e2 -> greeting.removeFromParent());

                    mainElement.appendChild(greeting);
                })
                .addEventData("element.textContent")
                .addEventData("event.clientX")
                .addEventData("event.clientY");

        Element helloWorldElement = ElementFactory.createDiv("Hello world");

        Set<String> spanClasses = helloWorldElement.getClassList();

        helloWorldElement.setProperty("id", "hello-world");
        spanClasses.add("hello");
        helloWorldEventRemover = helloWorldElement.addEventListener("click", e -> {
            if (helloWorldElement.getText().equals("Hello world")) {
                helloWorldElement.setText("Stop touching me!");
            } else {
                // We never get to this code as long as the event
                // removal actually works
                helloWorldElement.setText(helloWorldElement.getText() + " This might be your last warning!");
            }
            spanClasses.clear();
            helloWorldEventRemover.remove();
        });
        Style s = helloWorldElement.getStyle();
        s.set("color", "red");
        s.set("fontWeight", "bold");

        Element elementContainer = ElementFactory.createDiv();

        Element toRemove = ElementFactory.createDiv("To Remove").setAttribute("id", "to-remove");
        elementContainer.appendChild(toRemove);

        elementContainer.setAttribute("id", "addremovecontainer");
        Element addRemoveButton = ElementFactory.createButton("Add and remove element");
        addRemoveButton.setAttribute("id", "addremovebutton");

        addRemoveButton.addEventListener("click", e -> {
            // very basic usecase: append and then immediately remove
            Element div = ElementFactory.createDiv("foobar");
            elementContainer.appendChild(div);
            elementContainer.removeChild(div);

            elementContainer.removeChild(toRemove);
            elementContainer.appendChild(toRemove);

            // Now let's have two "add" operation and then two "remove"
            // operation so that removal has an addition right before which
            // targets different element
            Element div2 = ElementFactory.createDiv("foobar");
            elementContainer.appendChild(div);
            elementContainer.appendChild(div2);

            Element ok = ElementFactory.createDiv("OK");
            ok.setAttribute("id", "ok");
            elementContainer.appendChild(ok);

            elementContainer.removeChild(div);
            elementContainer.removeChild(div2);
        });

        mainElement.appendChild(helloWorldElement, button, input, addRemoveButton, elementContainer);
    }
}
