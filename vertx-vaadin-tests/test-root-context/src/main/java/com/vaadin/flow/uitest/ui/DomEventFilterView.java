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

import java.util.concurrent.atomic.AtomicReference;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.DebouncePhase;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.DomEventFilterView", layout = ViewTestLayout.class)
public class DomEventFilterView extends AbstractDivView {

    @Tag("input")
    public static class DebounceComponent extends Component {
        public Registration addInputListener(ComponentEventListener<InputEvent> listener, int debounceTimeout) {
            return getEventBus().addListener(InputEvent.class, listener, domReg -> domReg.debounce(debounceTimeout));
        }
    }

    @DomEvent("input")
    public static class InputEvent extends ComponentEvent<DebounceComponent> {
        private String value;

        public InputEvent(DebounceComponent source, boolean fromClient, @EventData("element.value") String value) {
            super(source, fromClient);
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final Element messages = new Element("div");

    public DomEventFilterView() {
        Element space = new Element("input");
        space.setAttribute("id", "space");

        space.addEventListener("keypress", e -> addMessage("Space listener triggered"))
                .setFilter("event.key == ' ' || event.key == 'Spacebar'");
        // The key is called 'Spacebar' on IE11

        Element debounce = new Element("input");
        debounce.setAttribute("id", "debounce");

        debounce.addEventListener(
                        "input", e -> addMessage("Trailing: " + e.getEventData().getString("element.value")))
                .debounce(1000)
                .addEventData("element.value");
        debounce.addEventListener(
                        "input", e -> addMessage("Leading: " + e.getEventData().getString("element.value")))
                .debounce(1000, DebouncePhase.LEADING);
        debounce.addEventListener(
                        "input", e -> addMessage("Throttle: " + e.getEventData().getString("element.value")))
                .throttle(1000);

        DebounceComponent component = new DebounceComponent();
        component.setId("debounce-component");
        component.addInputListener(e -> addMessage("Component: " + e.getValue()), 1000);

        messages.setAttribute("id", "messages");
        getElement().appendChild(space, debounce, component.getElement(), messages);

        // tests for#5090
        final AtomicReference<DomListenerRegistration> atomicReference = new AtomicReference<>();
        final Paragraph resultParagraph = new Paragraph();
        resultParagraph.setId("result-paragraph");

        NativeButton removalButton = new NativeButton("Remove DOM listener", event -> {
            resultParagraph.setText("REMOVED");
            atomicReference.get().remove();
        });
        removalButton.setId("listener-removal-button");

        Input listenerInput = new Input(ValueChangeMode.ON_CHANGE);
        listenerInput.setId("listener-input");

        /*
        The event.preventDefault() is here to make sure that the listener
         has been cleaned on the client-side as well. The server-side
         cleaning is not really in question.
         */
        ComponentUtil.addListener(
                listenerInput, KeyDownEvent.class, event -> resultParagraph.setText("A"), registration -> {
                    atomicReference.set(registration);
                    registration.setFilter("event.key === 'a' && " + "(event.preventDefault() || true)");
                });
        ComponentUtil.addListener(
                listenerInput,
                KeyDownEvent.class,
                event -> resultParagraph.setText("B"),
                registration -> registration.setFilter("event.key === 'b' && " + "(event.preventDefault() || true)"));

        add(listenerInput, removalButton, resultParagraph);
    }

    private void addMessage(String message) {
        Element element = new Element("div");
        element.setText(message);
        messages.appendChild(element);
    }
}
