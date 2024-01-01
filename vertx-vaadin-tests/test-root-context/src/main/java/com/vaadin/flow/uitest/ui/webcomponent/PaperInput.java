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
package com.vaadin.flow.uitest.ui.webcomponent;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("paper-input")
@NpmPackage(value = "@polymer/paper-input", version = "3.0.2")
@JsModule("@polymer/paper-input/paper-input.js")
public class PaperInput extends Component {
    private static final PropertyDescriptor<String, String> valueDescriptor =
            PropertyDescriptors.propertyWithDefault("value", "");

    public PaperInput() {
        // (this public no-arg constructor is required so that Flow can
        // instantiate beans of this type
        // when they are bound to template elements via the @Id() annotation)
    }

    public PaperInput(String value) {
        setValue(value);
    }

    @Synchronize("value-changed")
    public String getValue() {
        return get(valueDescriptor);
    }

    @Synchronize("invalid-changed")
    public String getInvalid() {
        return getElement().getProperty("invalid");
    }

    public void setValue(String value) {
        set(valueDescriptor, value);
    }
}
