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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import elemental.json.Json;
import elemental.json.JsonObject;

@Route("com.vaadin.flow.uitest.ui.HistoryView")
public class HistoryView extends AbstractDivView {

    private final Element stateJsonInput = createSynchronizedInput("state");
    private final Element locationInput = createSynchronizedInput("location");

    public HistoryView() {
        History history = getPage().getHistory();

        addRow(Element.createText("State to set (JSON) "), stateJsonInput);
        addRow(Element.createText("Location to set "), locationInput);

        addRow(
                createStateButton("pushState", history::pushState),
                createStateButton("replaceState", history::replaceState));

        addRow(createActionButton("back", history::back), createActionButton("forward", history::forward));

        addRow(createActionButton("clear", this::clear));

        history.setHistoryStateChangeHandler(e -> {
            addStatus("New location: " + e.getLocation().getPath());

            e.getState().ifPresent(state -> addStatus("New state: " + state.toJson()));
        });
    }

    private void clear() {
        while (true) {
            Element lastChild = getElement().getChild(getElement().getChildCount() - 1);
            if (lastChild.getClassList().contains("status")) {
                lastChild.removeFromParent();
            } else {
                return;
            }
        }
    }

    private Element createActionButton(String text, Command command) {
        return createButton(text, e -> command.execute());
    }

    private Element createStateButton(String text, SerializableBiConsumer<JsonObject, String> stateUpdater) {
        return createButton(text, e -> {
            String stateJsonString = stateJsonInput.getProperty("value", "");
            JsonObject stateJson;
            if (stateJsonString.isEmpty()) {
                stateJson = null;
            } else {
                stateJson = Json.parse(stateJsonString);
            }

            String location = locationInput.getProperty("value", "");

            stateJsonInput.setProperty("value", "");
            locationInput.setProperty("value", "");

            stateUpdater.accept(stateJson, location);
        });
    }

    private Element addRow(Element... elements) {
        Element row = ElementFactory.createDiv().appendChild(elements);
        getElement().appendChild(row);
        return row;
    }

    private void addStatus(String text) {
        Element statusRow = addRow(Element.createText(text));
        statusRow.getClassList().add("status");
    }

    private static Element createButton(String id, ComponentEventListener<ClickEvent<NativeButton>> listener) {
        return createButton(id, id, listener).getElement();
    }

    private static Element createSynchronizedInput(String id) {
        Element element = ElementFactory.createInput().setAttribute("id", id);
        element.addPropertyChangeListener("value", "change", event -> {});
        return element;
    }
}
