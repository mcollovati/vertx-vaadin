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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dnd.DragEndEvent;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DragStartEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.DnDCustomComponentView", layout = ViewTestLayout.class)
public class DnDCustomComponentView extends Div {

    private final Div dropTarget;

    public class DraggableItem extends Div implements DragSource<DraggableItem> {

        private final Div dragHandle;

        public DraggableItem(EffectAllowed effectAllowed) {
            dragHandle = new Div();
            dragHandle.setHeight("50px");
            dragHandle.setWidth("80px");
            dragHandle.getStyle().set("background", "#000 ");
            dragHandle.getStyle().set("margin", "5 10px");
            dragHandle.getStyle().set("display", "inline-block");

            setDraggable(true);
            setDragData(effectAllowed);
            addDragStartListener(DnDCustomComponentView.this::onDragStart);
            addDragEndListener(DnDCustomComponentView.this::onDragEnd);

            setHeight("50px");
            setWidth("200px");
            getStyle().set("border", "1px solid black");
            getStyle().set("display", "inline-block");

            add(dragHandle, new Span(effectAllowed.toString()));
        }

        @Override
        public Element getDraggableElement() {
            return dragHandle.getElement();
        }
    }

    public DnDCustomComponentView() {
        Stream.of(EffectAllowed.values()).map(DraggableItem::new).forEach(this::add);

        dropTarget = new Div();
        dropTarget.add(new Text("Drop Here"));
        dropTarget.setWidth("200px");
        dropTarget.setHeight("200px");
        dropTarget.getStyle().set("border", "solid 1px pink");
        add(dropTarget);

        DropTarget.create(dropTarget).addDropListener(event -> event.getSource()
                .add(new Span(event.getDragData().get().toString())));
    }

    private void onDragStart(DragStartEvent<DraggableItem> event) {
        dropTarget.getStyle().set("background-color", "lightgreen");
    }

    private void onDragEnd(DragEndEvent<DraggableItem> event) {
        dropTarget.getStyle().remove("background-color");
    }
}
