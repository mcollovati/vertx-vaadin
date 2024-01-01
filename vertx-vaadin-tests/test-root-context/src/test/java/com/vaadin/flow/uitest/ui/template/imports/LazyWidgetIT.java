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
package com.vaadin.flow.uitest.ui.template.imports;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

/**
 * This test is intended to check that templates work as Polymer elements even
 * if they're lazy loaded.
 *
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class LazyWidgetIT extends ChromeBrowserTest {

    @Test
    public void lazyLoadedPolymerTemplateWorksAsElement() {
        open();
        waitForElementVisible(By.id("template")); // template is lazy loaded,
        // need some time to load

        TestBenchElement template = $(TestBenchElement.class).id("template");
        String input = "InputMaster";
        Assert.assertFalse(
                "No greeting should be present before we press the button",
                template.$("*").attribute("id", "greeting").exists());

        template.$(TestBenchElement.class).id("input").sendKeys(input);
        template.$(TestBenchElement.class).id("button").click();

        Assert.assertEquals(
                "Greeting is different from expected",
                String.format(LazyWidgetView.GREETINGS_TEMPLATE, input),
                template.$(TestBenchElement.class).id("greeting").getText());
    }
}
