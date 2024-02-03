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

import java.util.EventObject;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;

@Route("uievents")
public class UIEventsView extends Div implements AfterNavigationObserver {

    public static final String POLL_FROM_CLIENT = "POLL_FROM_CLIENT";
    public static final String NAVIGATION_EVENTS = "NAVIGATION_EVENTS";

    @NormalUIScoped
    public static class PollObserver {
        private void showPollEvent(@Observes PollEvent pollEvent) {
            UI ui = pollEvent.getSource();

            List<HasElement> chain = ui.getInternals().getActiveRouterTargetsChain();

            HasElement leaf = chain.get(chain.size() - 1);
            if (leaf instanceof UIEventsView) {

                final Label poll = new Label(pollEvent.isFromClient() + "");
                poll.setId(POLL_FROM_CLIENT);

                ((UIEventsView) leaf).add(new Div(poll));
            }
        }
    }

    @Inject
    private NavigationObserver navigationObserver;

    @PostConstruct
    private void init() {
        UI.getCurrent().setPollInterval(500);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        showNavigationEvents();
    }

    private void showNavigationEvents() {
        Div events = new Div();
        events.setId(NAVIGATION_EVENTS);
        List<EventObject> navigationEvents = navigationObserver.getNavigationEvents();
        navigationEvents.stream()
                .map(event -> new Label(event.getClass().getSimpleName()))
                .forEach(events::add);
        add(events);
    }
}
