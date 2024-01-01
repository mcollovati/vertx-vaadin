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
package com.vaadin.flow.uitest.ui.scroll;

import java.util.function.BiConsumer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;
import elemental.json.JsonValue;

@Route(value = "com.vaadin.flow.uitest.ui.scroll.PushStateScrollView", layout = ViewTestLayout.class)
public class PushStateScrollView extends AbstractDivView {
    public PushStateScrollView() {
        Element filler = ElementFactory.createDiv(
                "Pushing or replacing history state should not affect the scroll position. Scroll down for buttons to click.");
        filler.getStyle().set("height", "150vh");

        History history = UI.getCurrent().getPage().getHistory();

        getElement()
                .appendChild(
                        filler,
                        createButton("push", history::pushState),
                        createButton("replace", history::replaceState));
    }

    private static Element createButton(String name, BiConsumer<JsonValue, String> action) {
        String location = PushStateScrollView.class.getName() + "/" + name;

        Element button = ElementFactory.createButton(name);

        button.setAttribute("id", name);
        button.addEventListener("click", e -> action.accept(null, location));

        return button;
    }
}
