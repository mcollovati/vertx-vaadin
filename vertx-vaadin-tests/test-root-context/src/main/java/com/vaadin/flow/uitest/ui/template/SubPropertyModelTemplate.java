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

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.ModelItem;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.template.SubPropertyModelTemplate.SubPropertyModel;

@Tag("sub-property-model")
@JsModule("SubPropertyModel.js")
public class SubPropertyModelTemplate extends PolymerTemplate<SubPropertyModel> {

    public interface Status {
        void setMessage(String message);

        String getMessage();
    }

    public interface SubPropertyModel extends TemplateModel {
        void setStatus(Status status);
    }

    public SubPropertyModelTemplate() {
        setMessage("message");
    }

    @EventHandler
    private void update() {
        setMessage("Updated");
    }

    @EventHandler
    private void sync() {
        Div div = new Div();
        div.setId("synced-msg");
        div.setText(getStatus().getMessage());
        ((HasComponents) getParent().get()).add(div);
    }

    @EventHandler
    private void valueUpdated() {
        Div div = new Div();
        div.setId("value-update");
        div.setText(getStatus().getMessage());
        ((HasComponents) getParent().get()).add(div);
    }

    @EventHandler
    private void click(@ModelItem("status") Status statusItem) {
        Div div = new Div();
        div.setId("statusClick");
        div.setText(statusItem.getMessage());
        ((HasComponents) getParent().get()).add(div);
    }

    private void setMessage(String message) {
        getStatus().setMessage(message);
    }

    private Status getStatus() {
        return getModel().getProxy("status", Status.class);
    }
}
