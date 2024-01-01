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
package com.vaadin.flow.uitest.ui.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * Tests a scalability bug #5806 with adding many buttons to a view.
 */
@Tag("template-scalability-view")
@JsModule("template-scalability-view.js")
@Route(value = "com.vaadin.flow.uitest.ui.template.TemplateScalabilityView")
@PageTitle("Template scalability")
public class TemplateScalabilityView extends PolymerTemplate<TemplateModel> implements AfterNavigationObserver {

    public static final String COMPLETED = "completed";

    public static final int NUM_ITEMS = 50;

    @Id("content")
    private Div div;

    public TemplateScalabilityView() {
        setId("scalability-view");
    }

    private void generateChildren() {
        div.removeAll();
        for (int i = 0; i < NUM_ITEMS; ++i) {
            TemplateScalabilityPanel p = new TemplateScalabilityPanel("Panel " + i);
            div.add(p);
        }

        Div complete = new Div();
        complete.setId(COMPLETED);
        div.addComponentAsFirst(complete);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        generateChildren();
    }
}
