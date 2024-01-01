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

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.scroll.MultipleAnchorsView", layout = ViewTestLayout.class)
public class MultipleAnchorsView extends AbstractDivView {
    static final int NUMBER_OF_ANCHORS = 6;
    static final String ANCHOR_URL_ID_BASE = "anchorUrlId";
    static final String ANCHOR_DIV_ID_BASE = "anchorDivId";
    static final String ANCHOR_URL_BASE = MultipleAnchorsView.class.getCanonicalName() + '#' + ANCHOR_DIV_ID_BASE;

    public MultipleAnchorsView() {
        boolean isRouterLink = true;
        Div anchorDivContainer = new Div();

        for (int i = 0; i < NUMBER_OF_ANCHORS; i++) {
            String anchorDivId = ANCHOR_DIV_ID_BASE + i;
            Div anchorDiv = new Div();
            anchorDiv.setId(anchorDivId);
            anchorDiv.setText("I am an anchor div #" + i);

            Anchor anchorUrl = ScrollView.createAnchorUrl(
                    isRouterLink,
                    ANCHOR_URL_ID_BASE + i,
                    MultipleAnchorsView.class.getCanonicalName() + '#' + anchorDivId,
                    "Anchor url #" + i);
            isRouterLink = !isRouterLink;

            add(anchorUrl);
            anchorDivContainer.add(ScrollView.createSpacerDiv(200), anchorDiv);
        }

        add(ScrollView.createSpacerDiv(1000), anchorDivContainer, ScrollView.createSpacerDiv(1000));
    }
}
