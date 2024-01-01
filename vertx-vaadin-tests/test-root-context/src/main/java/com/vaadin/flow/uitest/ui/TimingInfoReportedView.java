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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.TimingInfoReportedView", layout = ViewTestLayout.class)
public class TimingInfoReportedView extends Div {

    // @formatter:off
    private static final String REPORT_TIMINGS = "var element = this; setTimeout(function() {"
            + "function report(array){ "
            + "var div = document.createElement('div');"
            + "div.className='log';"
            + "element.appendChild(div); "
            + "if (array.length != 5) { "
            + "  div.appendChild(document.createTextNode('ERROR: expected 5 values, got '+array.length())); "
            + "}"
            + "for (i = 0; i < array.length; i++) { "
            + "  var value = 0+array[i];"
            + "  if ( value <0 || value >10000) {"
            + "     div.appendChild(document.createTextNode('ERROR: expected value "
            + " to be between 0 and 10000, was '+value)); "
            + "     return; "
            + "  } "
            + "}"
            + "div.appendChild(document.createTextNode('Timings ok'));"
            + "}; "
            + "report(window.Vaadin.Flow.clients[Object.keys(window.Vaadin.Flow.clients).filter(k => k !== 'TypeScript')].getProfilingData());"
            + "},0);";
    // @formatter:on

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getElement().executeJs(REPORT_TIMINGS);
        NativeButton button = new NativeButton("test request");
        button.addClickListener(event -> getElement().executeJs(REPORT_TIMINGS));
        add(button);
    }
}
