/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;

@Route("com.vaadin.flow.uitest.ui.PopStateHandlerView")
public class PopStateHandlerView extends RouterLinkView {

    @Override
    protected void addLinks() {
        getElement()
                .appendChild(
                        createPushStateButtons("com.vaadin.flow.uitest.ui.PopStateHandlerUI/another/"),
                        ElementFactory.createParagraph(),
                        createPushStateButtons("com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/"),
                        ElementFactory.createParagraph(),
                        createPushStateButtons("com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#!/category/1"),
                        ElementFactory.createParagraph(),
                        createPushStateButtons("com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#!/category/2"),
                        ElementFactory.createParagraph(),
                        createPushStateButtons("com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#"));
    }

    protected Element createPushStateButtons(String target) {
        Element button = ElementFactory.createButton(target).setAttribute("id", target);
        button.addEventListener("click", e -> {})
                .addEventData("window.history.pushState(null, null, event.target.textContent)");
        return button;
    }
}
