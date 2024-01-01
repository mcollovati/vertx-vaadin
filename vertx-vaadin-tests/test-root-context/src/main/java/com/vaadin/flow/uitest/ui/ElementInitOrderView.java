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

import java.util.stream.Stream;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.ElementInitOrderView", layout = ViewTestLayout.class)
@JsModule("ElementInitOrder.js")
public class ElementInitOrderView extends AbstractDivView {
    public ElementInitOrderView() {
        NativeButton reattach = createButton("Reattach components", "reattach", event -> reattachElements());

        add(reattach, new Html("<br />"));

        reattachElements();
    }

    private void reattachElements() {
        Stream.of("init-order-polymer", "init-order-nopolymer")
                // Remove old child if present
                .peek(name -> getElement()
                        .getChildren()
                        .filter(child -> child.getTag().equals(name))
                        .findFirst()
                        .ifPresent(Element::removeFromParent))
                // Create and attach new child
                .map(ElementInitOrderView::createElement)
                .forEach(getElement()::appendChild);
    }

    private static Element createElement(String tag) {
        Element element = new Element(tag);
        element.appendChild(new Element("span"));
        element.getStyle().set("animationName", "style");
        element.getClassList().add("class");
        element.setAttribute("attribute", "attribute");
        element.setProperty("property", "property");
        return element;
    }
}
