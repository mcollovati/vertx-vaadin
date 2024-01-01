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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.NavigationTriggerView", layout = ViewTestLayout.class)
public class NavigationTriggerView extends AbstractDivView implements HasUrlParameter<String>, BeforeLeaveObserver {
    private static final String CLASS_NAME = NavigationTriggerView.class.getName();

    public NavigationTriggerView() {
        // Cannot use the RouterLink component since these views are not
        // registered as regular views.
        Element routerLink = ElementFactory.createRouterLink(CLASS_NAME + "/routerlink/", "Router link");
        routerLink.setAttribute("id", "routerlink");

        Element navigateButton = ElementFactory.createButton("UI.navigate");
        navigateButton.addEventListener("click", e -> getUI().get().navigate(CLASS_NAME + "/navigate"));
        navigateButton.setAttribute("id", "navigate");

        Element forwardButton = ElementFactory.createButton("forward");
        forwardButton.addEventListener("click", e -> getUI().get().navigate(NavigationTriggerView.class, "forward"));
        forwardButton.setAttribute("id", "forwardButton");

        Element rerouteButton = ElementFactory.createButton("reroute");
        rerouteButton.addEventListener("click", e -> getUI().get().navigate(NavigationTriggerView.class, "reroute"));
        rerouteButton.setAttribute("id", "rerouteButton");

        getElement().appendChild(routerLink, navigateButton, forwardButton, rerouteButton);
    }

    public static String buildMessage(String path, NavigationTrigger trigger, String parameter) {
        return "Navigated to " + path + " with trigger " + trigger.name() + " and parameter " + parameter;
    }

    private void addMessage(String message) {
        Element element = ElementFactory.createDiv(message);
        element.getClassList().add("message");
        getElement().appendChild(element);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        String location = event.getLocation().getPathWithQueryParameters();
        assert location.startsWith(CLASS_NAME);

        location = location.substring(CLASS_NAME.length());
        if (location.isEmpty()) {
            // For clarity in the message
            location = "/";
        }

        addMessage(buildMessage(location, event.getTrigger(), parameter));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (event.getLocation().getPath().endsWith("forward")) {
            event.forwardTo(CLASS_NAME, "forwarded");
        } else if (event.getLocation().getPath().endsWith("reroute")) {
            event.rerouteTo(CLASS_NAME, "rerouted");
        }
    }
}
