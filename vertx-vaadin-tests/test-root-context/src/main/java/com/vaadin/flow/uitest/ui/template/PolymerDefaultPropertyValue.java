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

import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.template.PolymerDefaultPropertyValue.MyModel;

@Tag("default-property")
@JsModule("PolymerDefaultPropertyValue.js")
public class PolymerDefaultPropertyValue extends PolymerTemplate<MyModel> {

    public interface MyModel extends TemplateModel {
        void setText(String text);

        void setName(String name);
    }

    private static final PropertyDescriptor<String, String> msgDescriptor =
            PropertyDescriptors.propertyWithDefault("message", "");

    public PolymerDefaultPropertyValue() {
        getModel().setText("foo");
        setMessage("updated-message");
    }

    @Synchronize("email-changed")
    public String getEmail() {
        return getElement().getProperty("email");
    }

    @Synchronize("message-changed")
    public String getMessage() {
        return get(msgDescriptor);
    }

    public void setMessage(String value) {
        set(msgDescriptor, value);
    }
}
