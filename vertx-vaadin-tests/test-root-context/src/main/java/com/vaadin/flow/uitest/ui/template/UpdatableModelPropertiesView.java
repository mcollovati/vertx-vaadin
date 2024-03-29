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

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.ClientUpdateMode;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Tag("updatable-model-properties")
@Route(value = "com.vaadin.flow.uitest.ui.template.UpdatableModelPropertiesView", layout = ViewTestLayout.class)
@JsModule("UpdatableModelProperties.js")
public class UpdatableModelPropertiesView extends PolymerTemplate<UpdatableModelPropertiesView.UpdatablePropertiesModel>
        implements HasComponents {

    public interface UpdatablePropertiesModel extends TemplateModel {

        @AllowClientUpdates(ClientUpdateMode.IF_TWO_WAY_BINDING)
        String getName();

        @AllowClientUpdates
        String getEmail();

        @AllowClientUpdates
        void setAge(int age);

        @AllowClientUpdates(ClientUpdateMode.DENY)
        void setText(String text);
    }

    public UpdatableModelPropertiesView() {
        setId("template");

        Label label = new Label();
        label.setId("property-value");
        add(label);

        getElement()
                .addPropertyChangeListener(
                        "name", event -> label.setText(getModel().getName()));
        getElement()
                .addPropertyChangeListener(
                        "email", event -> label.setText(getModel().getEmail()));
        getElement()
                .addPropertyChangeListener(
                        "age", event -> label.setText(getElement().getProperty("age")));
        getElement()
                .addPropertyChangeListener(
                        "text", event -> label.setText(getElement().getProperty("text")));
    }

    @EventHandler
    private void syncAge() {
        getElement().addPropertyChangeListener("age", "age-changed", event -> {});
    }

    @ClientCallable
    private void updateStatus() {
        getElement()
                .setProperty("updateStatus", "Update Done " + UUID.randomUUID().toString());
    }
}
