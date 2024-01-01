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
package com.vaadin.flow.uitest.ui;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

public class TestingServiceInitListener implements VaadinServiceInitListener {
    public static final String DYNAMICALLY_REGISTERED_ROUTE = "dynamically-registered-route";
    private static AtomicInteger initCount = new AtomicInteger();
    private static AtomicInteger requestCount = new AtomicInteger();
    private static Set<UI> notNavigatedUis = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private boolean redirected;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(this::handleUIInit);
        initCount.incrementAndGet();

        RouteConfiguration configuration = RouteConfiguration.forApplicationScope();

        if (!configuration.isPathAvailable(DYNAMICALLY_REGISTERED_ROUTE)) {
            configuration.setRoute(DYNAMICALLY_REGISTERED_ROUTE, DynamicallyRegisteredRoute.class);
        }

        event.addRequestHandler((session, request, response) -> {
            requestCount.incrementAndGet();
            return false;
        });
    }

    public static int getInitCount() {
        return initCount.get();
    }

    public static int getRequestCount() {
        return requestCount.get();
    }

    public static int getNotNavigatedUis() {
        return notNavigatedUis.size();
    }

    private void handleUIInit(UIInitEvent event) {
        notNavigatedUis.add(event.getUI());
        event.getUI().addBeforeEnterListener((BeforeEnterListener & Serializable) e -> {
            if (e.getNavigationTarget() != null) {
                notNavigatedUis.remove(e.getUI());
            }
            if (!redirected && ServiceInitListenersView.class.equals(e.getNavigationTarget())) {
                e.rerouteTo(e.getLocation().getPath(), 22);
                redirected = true;
            }
        });
    }
}
