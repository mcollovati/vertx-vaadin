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
package com.github.mcollovati.vertx.vaadin.quarkus.it.uievents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import jakarta.enterprise.event.Observes;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

@VaadinSessionScoped
public class NavigationObserver implements Serializable {

    private List<EventObject> navigationEvents = new ArrayList<>();

    private void onBeforeLeave(@Observes BeforeLeaveEvent event) {
        navigationEvents.add(event);
    }

    private void onBeforeEnter(@Observes BeforeEnterEvent event) {
        navigationEvents.add(event);
    }

    private void onAfterNavigation(@Observes AfterNavigationEvent event) {
        navigationEvents.add(event);
    }

    public List<EventObject> getNavigationEvents() {
        return navigationEvents;
    }
}
