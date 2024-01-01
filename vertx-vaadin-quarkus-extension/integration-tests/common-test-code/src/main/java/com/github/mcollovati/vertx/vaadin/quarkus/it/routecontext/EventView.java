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
package com.github.mcollovati.vertx.vaadin.quarkus.it.routecontext;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScoped;

@Route("event")
@RouteScoped
public class EventView extends Div {

    public static final String FIRE = "FIRE";
    public static final String OBSERVER_LABEL = "OBSERVER_LABEL";

    @RouteScoped
    @RouteScopeOwner(EventView.class)
    public static class ObserverLabel extends Label {
        private void onPrintEvent(@Observes PrintEvent printEvent) {
            setText(printEvent.getMessage());
        }
    }

    public static class PrintEvent {
        private final String message;

        public PrintEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Inject
    @RouteScopeOwner(EventView.class)
    private Label label;

    @Inject
    private Event<PrintEvent> printEventTrigger;

    @PostConstruct
    private void init() {
        label.setId(OBSERVER_LABEL);
        NativeButton fireBtn =
                new NativeButton("fire event", clickEvent -> printEventTrigger.fire(new PrintEvent("HELLO")));
        fireBtn.setId(FIRE);

        add(fireBtn, label);
    }
}
