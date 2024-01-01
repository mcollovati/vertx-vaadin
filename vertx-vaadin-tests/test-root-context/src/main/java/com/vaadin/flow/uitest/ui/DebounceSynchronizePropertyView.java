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
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.DebounceSynchronizePropertyView", layout = ViewTestLayout.class)
public class DebounceSynchronizePropertyView extends AbstractDebounceSynchronizeView {
    private final HtmlComponent input = new HtmlComponent("input");
    private final Element inputElement = input.getElement();

    public DebounceSynchronizePropertyView() {
        input.getElement().setAttribute("id", "input");

        Component eagerToggle = createModeToggle("Eager (every keypress)", "eager");
        Component filteredToggle = createModeToggle(
                "Filtered (even length)",
                "filtered",
                registration -> registration.setFilter("element.value.length % 2 === 0"));
        Component debounceToggle = createModeToggle(
                "Debounce (when typing pauses)", "debounce", registration -> registration.debounce(CHANGE_TIMEOUT));
        Component throttleToggle = createModeToggle(
                "Throttle (while typing)", "throttle", registration -> registration.throttle(CHANGE_TIMEOUT));

        add(eagerToggle, filteredToggle, debounceToggle, throttleToggle, input);
        addChangeMessagesDiv();
    }

    private Component createModeToggle(
            String caption, String id, SerializableConsumer<DomListenerRegistration> configurator) {
        Element checkbox = new Element("input");
        checkbox.setAttribute("type", "checkbox");
        checkbox.setAttribute("id", id);

        checkbox.addEventListener("change", new DomEventListener() {
                    private DomListenerRegistration registration = null;

                    @Override
                    public void handleEvent(DomEvent event) {
                        if (event.getEventData().getBoolean("element.checked")) {
                            assert registration == null;

                            registration = inputElement.addPropertyChangeListener(
                                    "value", "input", propertyChange -> addChangeMessage(propertyChange.getValue()));

                            configurator.accept(registration);
                        } else {
                            registration.remove();
                            registration = null;
                        }
                    }
                })
                .addEventData("element.checked");

        Label label = new Label(caption);
        label.getElement().insertChild(0, checkbox);
        label.getElement().getStyle().set("display", "block");
        return label;
    }

    // Shorthand without configuration to keep UI building code clean
    private Component createModeToggle(String caption, String id) {
        return createModeToggle(caption, id, ignore -> {});
    }
}
