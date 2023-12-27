/*
 * Copyright 2000-2021 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

        final NativeButton button = new NativeButton("set state",
                event -> state.setText(UISCOPED_STATE));
        button.setId(SETSTATE_BTN);

        add(button, state, new RouterLink(ROOT_LINK, UIContextRootView.class));
    }
}
