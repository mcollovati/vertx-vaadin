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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.ScriptInjectView", layout = ViewTestLayout.class)
public class ScriptInjectView extends AbstractDivView {

    static String[] values = new String[] {
        "</script foo>",
        "</Script>",
        "</SCRIPT >",
        "</SCRIPT>",
        "< / SCRIPT>",
        "</ SCRIPT>",
        "< / SCRIPT >",
        "</SCRIpT>"
    };

    public ScriptInjectView() {
        for (String value : values) {
            createInput(value);
        }
    }

    private void createInput(String endTag) {
        String string = getValue(endTag);
        Element input = ElementFactory.createInput();
        input.setAttribute("value", string);
        getElement().appendChild(input);
    }

    static String getValue(String endTag) {
        return endTag + "<script>alert('foo');>" + endTag;
    }
}
