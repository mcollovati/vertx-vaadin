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

import java.util.Locale;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.EventHandlerView", layout = ViewTestLayout.class)
@Tag("event-handler")
@JsModule("EventHandler.js")
public class EventHandlerView extends PolymerTemplate<TemplateModel> {
    public EventHandlerView() {
        setId("template");
    }

    @EventHandler
    private void handleClick() {
        Element label = ElementFactory.createLabel("Event handler is invoked");
        label.setAttribute("id", "event-handler-result");
        getParent().get().getElement().appendChild(label);
    }

    @EventHandler
    private void sendData(
            @EventData("event.button") int button,
            @EventData("event.type") String type,
            @EventData("event.srcElement.tagName") String tag) {
        Element container = ElementFactory.createDiv();
        container.appendChild(ElementFactory.createDiv("Received event from the client with the data:"));
        container.appendChild(ElementFactory.createDiv("button: " + button));
        container.appendChild(ElementFactory.createDiv("type: " + type));
        container.appendChild(ElementFactory.createDiv("tag: " + tag.toLowerCase(Locale.ENGLISH)));
        container.setAttribute("id", "event-data");
        getParent().get().getElement().appendChild(container);
    }

    @EventHandler
    private void overriddenClick(@EventData("event.result") String result) {
        Element label = ElementFactory.createLabel("Overridden server event was invoked with result: " + result);
        label.setAttribute("id", "overridden-event-handler-result");
        getParent().get().getElement().appendChild(label);
    }

    @ClientCallable
    private void handleClientCall(String msg, boolean enabled) {
        Element div = ElementFactory.createDiv("Call from client, message: " + msg + ", " + enabled);
        div.setAttribute("id", "client-call");
        getParent().get().getElement().appendChild(div);
    }
}
