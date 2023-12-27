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
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.UUID;

import com.github.mcollovati.vertx.vaadin.quarkus.it.Counter;
import com.vaadin.flow.component.UI;
import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;

@NormalUIScoped
public class UIScopedBean {

    private final String id = UUID.randomUUID().toString();

    public static final String CREATE_COUNTER_KEY = UIScopedBean.class.getName()
            + "Create";
    public static final String DESTROY_COUNTER_KEY = UIScopedBean.class
            .getName() + "Destroy";

    private int uiId;

    @Inject
    private Counter counter;

    public String getId() {
        return id;
    }

    @PostConstruct
    void postConstruct() {
        uiId = UI.getCurrent().getUIId();
        counter.increment(CREATE_COUNTER_KEY + uiId);
    }

    @PreDestroy
    void preDestroy() {
        counter.increment(DESTROY_COUNTER_KEY + uiId);
    }
}
