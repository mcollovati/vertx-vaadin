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
package com.vaadin.flow.uitest.ui;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@Route(value = "com.vaadin.flow.uitest.ui.PreserveOnRefreshView")
@PreserveOnRefresh
public class PreserveOnRefreshView extends Div {

    static final String COMPONENT_ID = "contents";
    static final String NOTIFICATION_ID = "notification";
    static final String ATTACHCOUNTER_ID = "attachcounter";

    private int attached = 0;
    private final Div attachCounter;

    public PreserveOnRefreshView() {
        // create unique content for this instance
        final String uniqueId = Long.toString(new Random().nextInt());

        final Div componentId = new Div();
        componentId.setId(COMPONENT_ID);
        componentId.setText(uniqueId);
        add(componentId);

        // add an element to keep track of number of attach events
        attachCounter = new Div();
        attachCounter.setId(ATTACHCOUNTER_ID);
        attachCounter.setText("0");
        add(attachCounter);

        // also add an element as a separate UI child. This is expected to be
        // transferred on refresh (mimicking dialogs and notifications)
        final Element looseElement = new Element("div");
        looseElement.setProperty("id", NOTIFICATION_ID);
        looseElement.setText(uniqueId);
        UI.getCurrent().getElement().insertChild(0, looseElement);

        StreamResource resource =
                new StreamResource("filename", () -> new ByteArrayInputStream("foo".getBytes(StandardCharsets.UTF_8)));
        Anchor download = new Anchor("", "Download file");
        download.setHref(resource);
        download.setId("thelink");
        add(download);
    }

    @Override
    protected void onAttach(AttachEvent event) {
        attached += 1;
        attachCounter.setText(Integer.toString(attached));
    }
}
