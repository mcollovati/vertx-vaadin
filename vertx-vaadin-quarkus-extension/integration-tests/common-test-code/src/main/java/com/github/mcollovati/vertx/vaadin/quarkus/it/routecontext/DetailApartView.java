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
import javax.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;

@RouteScoped
@Route(value = "apart", layout = MasterView.class)
public class DetailApartView extends AbstractCountedView
        implements AfterNavigationObserver {

    public static final String MASTER = "master";
    public static final String BEAN_LABEL = "BEAN_LABEL";

    @Inject
    @RouteScopeOwner(DetailApartView.class)
    ApartBean apartBean;
    private Label apartLabel;

    @PostConstruct
    private void init() {
        apartLabel = new Label();
        apartLabel.setId(BEAN_LABEL);
        apartBean.setData("APART");
        add(apartLabel, new Div(new RouterLink(MASTER, MasterView.class)));
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        apartLabel.setText(apartBean.getData());
    }

}
