/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.quarkus.it.routecontext;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends Div implements RouterLayout {

    public static final String UIID = "UIID";

    public static final String PRESERVE = "preserve";
    public static final String INVALID = "invalid";
    public static final String PARENT_NO_OWNER = "parent-no-owner";
    public static final String CHILD_NO_OWNER = "child-no-owner";

    private Label uiIdLabel;

    public MainLayout() {
        add(
                new RouterLink(PRESERVE, PreserveOnRefreshView.class),
                new RouterLink(INVALID, InvalidView.class),
                new RouterLink(PARENT_NO_OWNER, ParentNoOwnerView.class),
                new RouterLink(CHILD_NO_OWNER, ChildNoOwnerView.class));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (uiIdLabel != null) {
            remove(uiIdLabel);
        }
        uiIdLabel = new Label(attachEvent.getUI().getUIId() + "");
        uiIdLabel.setId(UIID);
        add(uiIdLabel);
    }
}
