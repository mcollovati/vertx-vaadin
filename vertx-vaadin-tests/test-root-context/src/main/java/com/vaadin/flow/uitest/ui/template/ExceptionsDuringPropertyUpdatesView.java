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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.template.ExceptionsDuringPropertyUpdatesView.ExceptionsDuringPropertyUpdatesModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Tag("exceptions-property-update")
@Route(value = "com.vaadin.flow.uitest.ui.template.ExceptionsDuringPropertyUpdatesView", layout = ViewTestLayout.class)
@JsModule("ExceptionsDuringPropertyUpdates.js")
public class ExceptionsDuringPropertyUpdatesView extends PolymerTemplate<ExceptionsDuringPropertyUpdatesModel>
        implements HasComponents {

    public interface ExceptionsDuringPropertyUpdatesModel extends TemplateModel {
        void setText(String text);

        @AllowClientUpdates
        String getText();

        void setName(String name);

        @AllowClientUpdates
        String getName();

        void setTitle(String title);

        @AllowClientUpdates
        String getTitle();
    }

    public ExceptionsDuringPropertyUpdatesView() {
        Div msg = new Div();
        msg.setId("message");

        add(msg);

        getModel().setText("a");

        getElement().addPropertyChangeListener("text", event -> {
            throw new RuntimeException("Intentional exception in property sync handler for 'text'");
        });
        getElement().addPropertyChangeListener("title", event -> {
            throw new IllegalStateException("Intentional exception in property sync handler for 'title'");
        });
        getElement()
                .addPropertyChangeListener(
                        "name",
                        event -> msg.setText("Name is updated to " + getModel().getName()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            attachEvent.getSession().setErrorHandler(e -> {
                Div div = new Div(new Text("An error occurred: " + e.getThrowable()));
                div.addClassName("error");
                add(div);
            });
        }
    }
}
