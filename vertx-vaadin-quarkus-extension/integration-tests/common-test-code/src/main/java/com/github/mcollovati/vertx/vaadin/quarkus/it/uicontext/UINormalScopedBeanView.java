/*
 * The MIT License
 * Copyright © 2000-2018 Marco Collovati (mcollovati@gmail.com)
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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

@Route("normalscopedbean")
public class UINormalScopedBeanView extends Div {

    public static final String UIID_LABEL = "UIID_LABEL";

    @Inject
    private SessionScopedUIidService sessionScopedUIidService;

    @PostConstruct
    private void init() {
        final Label label = new Label(sessionScopedUIidService.getUiIdStr());
        label.setId(UIID_LABEL);
        add(label);
    }

    @NormalUIScoped
    public static class NormalUIScopedUIidService {
        private String uiIdStr;

        @PostConstruct
        public void init() {
            uiIdStr = UI.getCurrent().getUIId() + "";
        }

        public String getUiIdStr() {
            return uiIdStr;
        }
    }

    @VaadinSessionScoped
    public static class SessionScopedUIidService {
        @Inject
        private NormalUIScopedUIidService normalUIScopedUIidService;

        public String getUiIdStr() {
            return normalUIScopedUIidService.getUiIdStr();
        }
    }
}
