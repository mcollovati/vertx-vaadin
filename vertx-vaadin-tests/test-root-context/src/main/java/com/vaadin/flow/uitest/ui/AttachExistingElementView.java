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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.dom.ChildElementConsumer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.Node;
import com.vaadin.flow.dom.ShadowRoot;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.AttachExistingElementView", layout = ViewTestLayout.class)
public class AttachExistingElementView extends AbstractDivView {

    private Label attachedLabel;

    private class NonExistingElementCallback implements ChildElementConsumer {

        @Override
        public void accept(Element child) {
            throw new IllegalStateException();
        }

        @Override
        public void onError(Node<?> parent, String tag, Element previousSibling) {
            Div div = new Div();
            div.setText("Non-exisint element error");
            div.setId("non-existing-element");
            add(div);
        }
    }

    @Override
    protected void onShow() {
        setId("root-div");
        add(createButton("Attach label", "attach-label", event -> getElement()
                .getStateProvider()
                .attachExistingElement(getElement().getNode(), "label", null, this::handleLabel)));
        add(createButton("Attach Header", "attach-header", event -> getElement()
                .getStateProvider()
                .attachExistingElement(getElement().getNode(), "h1", null, this::handleHeader)));

        Div div = new Div();
        div.setId("element-with-shadow");
        ShadowRoot shadowRoot = div.getElement().attachShadow();

        add(createButton("Attach label in shadow", "attach-label-inshadow", event -> shadowRoot
                .getStateProvider()
                .attachExistingElement(shadowRoot.getNode(), "label", null, this::handleLabelInShadow)));

        add(createButton("Attach non-existing element", "non-existing-element", event -> getElement()
                .getStateProvider()
                .attachExistingElement(getElement().getNode(), "image", null, new NonExistingElementCallback())));

        add(div);
        getPage().executeJs("$0.appendChild(document.createElement('label'));", shadowRoot);

        getPage()
                .executeJs(
                        "$0.appendChild(document.createElement('span')); $0.appendChild(document.createElement('label'));"
                                + "$0.appendChild(document.createElement('h1'));",
                        getElement());
    }

    private void handleLabel(Element label) {
        attachedLabel = Component.from(label, Label.class);
        attachedLabel.setText("Client side label");
        attachedLabel.setId("label");

        add(AbstractDivView.createButton(
                "Attach the already attached label", "attach-populated-label", event -> getElement()
                        .getStateProvider()
                        .attachExistingElement(getElement().getNode(), "label", null, this::handleAttachedLabel)));

        add(createButton("Remove myself on the server side", "remove-self", event -> event.getSource()
                .getElement()
                .removeFromParent()));
    }

    private void handleAttachedLabel(Element label) {
        Element child = getElement()
                .getChild(attachedLabel.getParent().get().getElement().indexOfChild(attachedLabel.getElement()));
        // child is already populated label. The <code>label</code> should be
        // the same element
        if (child.equals(label)) {
            child.getClassList().add("already-populated");
        } else {
            child.getClassList().add("new-label");
        }
    }

    private void handleHeader(Element header) {
        H1 lbl = Component.from(header, H1.class);
        lbl.setText("Client side header");
        lbl.setId("h1-header");
    }

    private void handleLabelInShadow(Element label) {
        Label lbl = Component.from(label, Label.class);
        lbl.setText("Client side label in shadow root");
        lbl.setId("label-in-shadow");
    }
}
