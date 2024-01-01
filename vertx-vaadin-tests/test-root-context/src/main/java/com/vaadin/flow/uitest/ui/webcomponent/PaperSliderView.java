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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.webcomponent.PaperSliderView", layout = ViewTestLayout.class)
public class PaperSliderView extends AbstractDivView {
    static final String VALUE_TEXT_ID = "valueText";
    static final String CHANGE_VALUE_ID = "changeValue";
    static final int INITIAL_VALUE = 75;
    static final int UPDATED_VALUE = 50;

    public PaperSliderView() {
        Div valueText = new Div();
        valueText.setId(VALUE_TEXT_ID);
        PaperSlider paperSlider = new PaperSlider();
        paperSlider.setPin(true);
        paperSlider.addValueChangeListener(e -> {
            String text = "Value: " + e.getSource().getValue();
            text += " (set on " + (e.isFromClient() ? "client" : "server") + ')';
            valueText.setText(text);
        });
        paperSlider.setValue(INITIAL_VALUE);
        add(
                paperSlider,
                valueText,
                createButton(
                        "Set value to " + UPDATED_VALUE, CHANGE_VALUE_ID, e -> paperSlider.setValue(UPDATED_VALUE)));
    }
}
