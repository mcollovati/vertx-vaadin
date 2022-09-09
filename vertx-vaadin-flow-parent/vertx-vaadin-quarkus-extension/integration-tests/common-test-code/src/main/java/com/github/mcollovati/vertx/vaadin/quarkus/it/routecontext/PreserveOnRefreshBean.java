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
package com.github.mcollovati.vertx.vaadin.quarkus.it.routecontext;

import javax.annotation.PostConstruct;
import java.util.UUID;

import com.github.mcollovati.vertx.quarkus.annotation.NormalRouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;

@NormalRouteScoped
@RouteScopeOwner(PreserveOnRefreshView.class)
public class PreserveOnRefreshBean extends AbstractCountedBean {

    @PostConstruct
    private void init() {
        setData(UUID.randomUUID().toString());
    }

}