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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

public class TemplateMappingDetectorIT extends ChromeBrowserTest {

    @Test
    public void regularTemplate_mappedComponentsAreMarkedAsSuch() {
        open();

        TestBenchElement container = $("template-mapping-detector").first();
        assertMappedComponentsAreMarkedProperly(container, false);
    }

    @Test
    public void templateInTemplate_mappedComponentsAreMarkedAsSuch() {
        open();

        TestBenchElement parentTemplate = $("template-mapping-detector-parent").first();
        TestBenchElement container = parentTemplate.$(TestBenchElement.class).id("detector");

        assertMappedComponentsAreMarkedProperly(container, true);
    }

    @Test
    public void composite_mappedComponentsAreMarkedAsSuch() {
        open();

        TestBenchElement container = $(TestBenchElement.class).id("composite");
        assertMappedComponentsAreMarkedProperly(container, false);
    }

    private void assertMappedComponentsAreMarkedProperly(TestBenchElement container, boolean templateInTemplate) {
        TestBenchElement mappedComponent = container.$(TestBenchElement.class).id("detector1");
        Assert.assertEquals("Template mapped: true", mappedComponent.getText());

        TestBenchElement standaloneComponent =
                container.$(TestBenchElement.class).id("detector2");

        Assert.assertEquals("Template mapped: false", standaloneComponent.getText());

        TestBenchElement standaloneComposite =
                container.$(TestBenchElement.class).id("detector3");

        Assert.assertEquals("Composite template mapped: false Template mapped: false", standaloneComposite.getText());

        TestBenchElement theTemplateItself = container.$(TestBenchElement.class).id("detector4");

        Assert.assertEquals("The template itself: " + templateInTemplate, theTemplateItself.getText());
    }
}
