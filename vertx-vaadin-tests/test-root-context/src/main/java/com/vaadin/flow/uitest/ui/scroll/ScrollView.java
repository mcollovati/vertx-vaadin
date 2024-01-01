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

@Route(value = "com.vaadin.flow.uitest.ui.scroll.ScrollView", layout = ViewTestLayout.class)
public class ScrollView extends AbstractDivView {
    private static final String ROUTER_LINK_ATTRIBUTE_NAME = "router-link";

    static final String ANCHOR_DIV_ID = "anchorDivId";
    static final String ANCHOR_URL = ScrollView.class.getCanonicalName() + '#' + ANCHOR_DIV_ID;
    static final String SIMPLE_ANCHOR_URL_ID = "simpleAnchorUrlId";
    static final String ROUTER_ANCHOR_URL_ID = "routerAnchorUrlId";
    static final String TRANSITION_URL_ID = "transitionUrlId";

    public ScrollView() {
        Div anchorDiv = new Div();
        anchorDiv.setText("I'm the anchor div");
        anchorDiv.setId(ANCHOR_DIV_ID);

        add(
                createSpacerDiv(300),
                anchorDiv,
                createSpacerDiv(2000),
                createAnchorUrl(false, SIMPLE_ANCHOR_URL_ID, ANCHOR_URL, "Go to anchor div (simple link)"),
                createAnchorUrl(true, ROUTER_ANCHOR_URL_ID, ANCHOR_URL, "Go to anchor div (router-link)"),
                createAnchorUrl(
                        true,
                        TRANSITION_URL_ID,
                        LongToOpenView.class.getCanonicalName(),
                        "Go to different page (router-link)"));
    }

    static Anchor createAnchorUrl(boolean isRouterLink, String id, String url, String text) {
        Anchor result = new Anchor(url, text);
        result.getElement().setAttribute(ROUTER_LINK_ATTRIBUTE_NAME, isRouterLink);
        result.getStyle().set("display", "block");
        result.setId(id);
        return result;
    }

    static Div createSpacerDiv(int heightPx) {
        Div spacer = new Div();
        spacer.setHeight(heightPx + "px");
        return spacer;
    }
}
