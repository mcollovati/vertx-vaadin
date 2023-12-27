/*
 * Copyright 2000-2018 Vaadin Ltd.
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
import javax.inject.Inject;

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
