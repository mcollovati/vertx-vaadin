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

import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/* https://github.com/vaadin/flow/issues/6054 */
@Route(value = "com.vaadin.flow.uitest.ui.DnDAttachDetachView")
public class DnDAttachDetachView extends Div {

    public static final String DRAGGABLE_ID = "draggable";
    public static final String VIEW_1_ID = "view1";
    public static final String VIEW_2_ID = "view2";
    public static final String SWAP_BUTTON_ID = "swap-button";
    public static final String MOVE_BUTTON_ID = "move-button";
    private Div buttonSwitchViews = new Div();
    private Div buttonRemoveAdd = new Div();
    private Div view1 = new Div();
    private Div view2 = new Div();
    private int counter = 0;

    public DnDAttachDetachView() {
        setSizeFull();

        buttonSwitchViews.setText("Click to detach OR attach");
        buttonSwitchViews.setId(SWAP_BUTTON_ID);
        buttonSwitchViews.getStyle().set("border", "1px solid black");
        buttonSwitchViews.setHeight("20px");
        buttonSwitchViews.setWidth("200px");

        buttonRemoveAdd.setText("Click to detach AND attach");
        buttonRemoveAdd.setId(MOVE_BUTTON_ID);
        buttonRemoveAdd.getStyle().set("border", "1px solid black");
        buttonRemoveAdd.setHeight("20px");
        buttonRemoveAdd.setWidth("200px");

        add(buttonSwitchViews, buttonRemoveAdd);

        Div div = new Div();
        div.setText("Text To Drag");
        div.setId(DRAGGABLE_ID);
        div.getStyle().set("background-color", "grey");
        add(div);

        add(view1);
        view1.setWidth("500px");
        view1.setHeight("500px");
        view1.getStyle().set("background-color", "pink");
        view1.setId(VIEW_1_ID);

        view2.setWidth("500px");
        view2.setHeight("500px");
        view2.setId(VIEW_2_ID);

        // need to set the effect allowed and drop effect for the simulation
        DragSource<Div> dragSource = DragSource.create(div);
        dragSource.addDragStartListener(event -> add(new Span("Start: " + counter)));
        dragSource.setEffectAllowed(EffectAllowed.COPY);

        DropTarget<Div> dt = DropTarget.create(view1);
        dt.setDropEffect(DropEffect.COPY);

        buttonSwitchViews.addClickListener(event -> {
            if (getChildren().anyMatch(component -> component == view1)) {
                remove(div, view1);
                add(view2);
            } else {
                remove(view2);
                add(div, view1);
            }
        });
        buttonRemoveAdd.addClickListener(event -> {
            remove(div, view1);
            add(div, view1);
        });
        DropTarget.configure(view1).addDropListener(this::onDrop);
    }

    private void onDrop(DropEvent<Div> divDropEvent) {
        Span span = new Span("Drop: " + counter);
        span.setId("drop-" + counter);
        add(span);
        counter++;
    }
}
