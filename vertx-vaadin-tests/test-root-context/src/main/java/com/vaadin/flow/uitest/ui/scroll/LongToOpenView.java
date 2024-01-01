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
package com.vaadin.flow.uitest.ui.scroll;

import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.scroll.LongToOpenView", layout = ViewTestLayout.class)
public class LongToOpenView extends AbstractDivView {
    static final String BACK_BUTTON_ID = "backButton";
    static final String ANCHOR_LINK_ID = "anchorLinkId";

    public LongToOpenView() {
        Div div = new Div();
        div.setText("I am the long to open view");

        NativeButton back = createButton(
                "Back", BACK_BUTTON_ID, event -> getPage().getHistory().back());

        add(
                div,
                back,
                ScrollView.createAnchorUrl(true, ANCHOR_LINK_ID, ScrollView.ANCHOR_URL, "Anchor url to other view"));
    }

    @Override
    protected void onShow() {
        try {
            // Delay is added to check that we don't jump to the beginning of
            // the page right after we click a link to this page.
            // We should update our scroll position only after the new page is
            // loaded.
            Thread.sleep(TimeUnit.SECONDS.toMillis(3L));
        } catch (InterruptedException e) {
            throw new IllegalStateException("Not supposed to be interrupted", e);
        }
    }
}
