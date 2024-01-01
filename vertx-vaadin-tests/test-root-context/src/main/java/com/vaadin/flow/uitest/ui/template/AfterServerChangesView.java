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

import java.util.UUID;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.AfterServerChangesView", layout = ViewTestLayout.class)
public class AfterServerChangesView extends AbstractDivView {

    @Tag("after-server-changes")
    @JsModule("AfterServerChanges.js")
    public static class AfterServerChanges extends PolymerTemplate<Message> {

        @Override
        protected Message getModel() {
            return super.getModel();
        }
    }

    public AfterServerChangesView() {
        add(new AfterServerChanges());
        AfterServerChanges component = new AfterServerChanges();
        add(component);

        add(new OneWayPolymerBindingView());

        add(createButton("Remove the second component", "remove", event -> remove(component)));

        add(createButton("Update components", "update", event -> updateComponents()));
    }

    private void updateComponents() {
        getChildren()
                .filter(component -> component.getClass().equals(AfterServerChanges.class))
                .map(AfterServerChanges.class::cast)
                .forEach(component ->
                        component.getModel().setText(UUID.randomUUID().toString()));
    }
}
