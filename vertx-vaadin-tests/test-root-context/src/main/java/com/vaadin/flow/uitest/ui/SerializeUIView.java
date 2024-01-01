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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

@Route(value = "com.vaadin.flow.uitest.ui.SerializeUIView", layout = ViewTestLayout.class)
public class SerializeUIView extends AbstractDivView {
    public SerializeUIView() {
        Div label = new Div();
        label.setId("message");

        NativeButton button = createButton("Serialize", "serialize", event -> {
            UI ui = UI.getCurrent();
            try {
                byte[] serialize = SerializationUtils.serialize(ui);

                String result = serialize.length > 0 ? "Successfully serialized ui" : "Serialization failed";
                label.setText(result);
            } catch (SerializationException se) {
                label.setText(se.getMessage());
            }
        });

        add(label, button);
    }
}
