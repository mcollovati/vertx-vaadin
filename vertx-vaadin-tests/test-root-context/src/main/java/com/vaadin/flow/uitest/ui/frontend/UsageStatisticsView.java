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
package com.vaadin.flow.uitest.ui.frontend;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.uitest.ui.template.HiddenTemplateView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

// Devmode detector detects bundling based on whether polymer-element.html is loaded
@NpmPackage(value = "@vaadin/vaadin-development-mode-detector", version = "1.1.0")
@JsModule("@vaadin/vaadin-development-mode-detector/vaadin-development-mode-detector.js")
@Route(value = "com.vaadin.flow.uitest.ui.frontend.UsageStatisticsView", layout = ViewTestLayout.class)
public class UsageStatisticsView extends Div {
    public UsageStatisticsView() {
        NativeButton print = new NativeButton("Print usage statistics to the console", e -> {
            getUI().get()
                    .getPage()
                    .executeJs(
                            "var basket = localStorage.getItem('vaadin.statistics.basket'); if (basket) basket = JSON.parse(basket); console.log(basket)");
        });
        NativeButton clear = new NativeButton("Clear usage statistics", e -> {
            getUI().get().getPage().executeJs("localStorage.removeItem('vaadin.statistics.basket')");
        });
        NativeButton push = new NativeButton(
                "Enable push", e -> getUI().get().getPushConfiguration().setPushMode(PushMode.AUTOMATIC));
        NativeButton template = new NativeButton("Use PolymerTemplate", e -> {
            add(new HiddenTemplateView());
        });

        add(
                new Text(
                        "View for manually testing usage statistics gathering for Flow features."
                                + " After a feature has been used, the page should be reloaded before verifying that usage info has been gathered."),
                new Div(print, clear, push, template));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        attachEvent.getUI().getPage().executeJs("window.Vaadin.runIfDevelopmentMode('vaadin-usage-statistics');");
    }
}
