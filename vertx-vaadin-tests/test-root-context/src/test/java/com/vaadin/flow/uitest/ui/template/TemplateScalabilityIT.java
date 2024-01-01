/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;
import org.junit.Assert;
import org.junit.Test;

public class TemplateScalabilityIT extends ChromeBrowserTest {

    @Test
    public void openPage_allButtonsRenderSuccessfully() {
        open();

        waitUntil(input -> {
            TestBenchElement view = $("*").id("scalability-view");
            return view.$("*")
                    .attribute("id", TemplateScalabilityView.COMPLETED)
                    .exists();
        });

        TestBenchElement viewTemplate = $("*").id("scalability-view");
        int buttons = viewTemplate.$("template-scalability-panel").all().size();

        Assert.assertEquals(
                "Template should have created " + TemplateScalabilityView.NUM_ITEMS + " panels with buttons.",
                TemplateScalabilityView.NUM_ITEMS,
                buttons);

        checkLogsForErrors();
    }

    @Element("template-scalability-panel")
    public class ScalabilityPanelElement extends TestBenchElement {}

    @Element("template-scalability-view")
    public class ScalabilityViewElement extends TestBenchElement {}
}
