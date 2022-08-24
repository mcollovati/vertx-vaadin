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

package com.github.mcollovati.vertx.vaadin.quarkus.it.service;

import javax.enterprise.event.Observes;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.communication.IndexHtmlResponse;

public class BootstrapCustomizer {

    public static final String APPENDED_ID = "TEST_ID";
    public static final String APPENDED_TXT = "By Test";

    private void onServiceInit(@Observes ServiceInitEvent serviceInitEvent) {
        serviceInitEvent.addIndexHtmlRequestListener(this::modifyBootstrapPage);
    }

    private void modifyBootstrapPage(IndexHtmlResponse response) {
        response.getDocument().body()
                .append("<p id='" + APPENDED_ID + "'>" + APPENDED_TXT + "</p>");
    }
}
