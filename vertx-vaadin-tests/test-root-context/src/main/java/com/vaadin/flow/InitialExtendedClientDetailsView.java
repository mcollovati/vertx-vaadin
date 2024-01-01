/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("com.vaadin.flow.InitialExtendedClientDetailsView")
public class InitialExtendedClientDetailsView extends Div {

    public InitialExtendedClientDetailsView() {
        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            addSpan("screenWidth", details.getScreenWidth());
            addSpan("screenHeight", details.getScreenHeight());
            addSpan("windowInnerWidth", details.getWindowInnerWidth());
            addSpan("windowInnerHeight", details.getWindowInnerHeight());
            addSpan("bodyClientWidth", details.getBodyClientWidth());
            addSpan("bodyClientHeight", details.getBodyClientHeight());
            addSpan("timezoneOffset", details.getTimezoneOffset());
            addSpan("timeZoneId", details.getTimeZoneId());
            addSpan("rawTimezoneOffset", details.getRawTimezoneOffset());
            addSpan("DSTSavings", details.getDSTSavings());
            addSpan("DSTInEffect", details.isDSTInEffect());
            addSpan("currentDate", details.getCurrentDate());
            addSpan("touchDevice", details.isTouchDevice());
            addSpan("devicePixelRatio", details.getDevicePixelRatio());
            addSpan("windowName", details.getWindowName());
        });
    }

    private void addSpan(String name, Object value) {
        Span span = new Span(value.toString());
        span.setId(name);
        add(span);
    }
}
