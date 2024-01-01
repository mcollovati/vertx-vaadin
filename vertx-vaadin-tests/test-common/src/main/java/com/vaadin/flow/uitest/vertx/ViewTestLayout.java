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
package com.vaadin.flow.uitest.vertx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinService;

public class ViewTestLayout extends Div implements RouterLayout, AfterNavigationObserver {

    private Element element = ElementFactory.createDiv();
    private Element viewContainer = ElementFactory.createDiv();
    private Element viewSelect = ElementFactory.createSelect();

    @Route(value = "", layout = ViewTestLayout.class)
    public static class BaseNavigationTarget extends Div {
        public BaseNavigationTarget() {
            setText(this.getClass().getSimpleName());
            setId("name-div");
        }
    }

    public ViewTestLayout() {

        element.setAttribute("id", "main-layout");

        List<Class<? extends Component>> classes = new ArrayList<>(
                // ViewTestVerticle.getViewLocator().getAllViewClasses()
                TestBootVerticle.getViewLocator(VaadinService.getCurrent()).getAllViewClasses());

        classes.removeIf(e -> !e.getName().endsWith("View"));

        Comparator<Class<? extends Component>> comparator = Comparator.comparing(Class::getName);
        Collections.sort(classes, comparator);

        String lastPackage = "";
        viewSelect.appendChild(ElementFactory.createOption());
        Element optionGroup = null;
        for (Class<? extends Component> c : classes) {
            if (!c.getPackage().getName().equals(lastPackage)) {
                lastPackage = c.getPackage().getName();
                optionGroup = new Element("optgroup");
                optionGroup.setAttribute("label", c.getPackage().getName().replaceAll("^.*\\.", ""));
                viewSelect.appendChild(optionGroup);
            }
            Element option = ElementFactory.createOption(c.getSimpleName()).setAttribute("value", c.getName());
            option.setAttribute("id", c.getSimpleName());

            optionGroup.appendChild(option);
        }

        // viewSelect.addPropertyChangeListener("value", "change", event -> {});
        viewSelect
                .addEventListener("change", e -> {
                    UI ui = UI.getCurrent();
                    ui.navigate(viewSelect.getProperty("value"));
                })
                .synchronizeProperty("value");

        element.appendChild(viewSelect, ElementFactory.createHr(), viewContainer);

        getElement().appendChild(element);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Defer value setting until all option elements have been attached
        UI.getCurrent()
                .getPage()
                .executeJs(
                        "setTimeout(function() {$0.value = $1}, 0)",
                        viewSelect,
                        event.getLocation().getPath());
    }
}
