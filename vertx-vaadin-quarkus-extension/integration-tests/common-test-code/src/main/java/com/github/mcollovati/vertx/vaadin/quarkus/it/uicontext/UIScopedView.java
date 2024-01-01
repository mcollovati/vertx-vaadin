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
package com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import com.github.mcollovati.vertx.quarkus.annotation.UIScoped;

@Route("uiscoped")
@UIScoped
public class UIScopedView extends Div {

    public static final String VIEWSTATE_LABEL = "VIEWSTATE_LABEL";
    public static final String SETSTATE_BTN = "SETSTATE_BTN";
    public static final String ROOT_LINK = "root view";
    public static final String UISCOPED_STATE = "UISCOPED_STATE";

    @PostConstruct
    private void init() {
        final Label state = new Label("");
        state.setId(VIEWSTATE_LABEL);

        final NativeButton button = new NativeButton("set state", event -> state.setText(UISCOPED_STATE));
        button.setId(SETSTATE_BTN);

        add(button, state, new RouterLink(ROOT_LINK, UIContextRootView.class));
    }
}
