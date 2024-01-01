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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class AttachExistingDomElementByIdIT extends ChromeBrowserTest {

    @Test
    public void elementsAreBoundOnTheServerSide() {
        open();

        assertTemplate("template", "bar", "Foo");
    }

    protected void assertTemplate(String id, String initialLabelText, String placeholder) {
        WebElement input = getInput(id);

        Assert.assertEquals(initialLabelText, getLabel(id).getText());

        Assert.assertEquals(placeholder, input.getAttribute("placeholder"));

        input.sendKeys("Harley!");
        input.sendKeys(Keys.TAB);

        Assert.assertEquals("Text from input Harley!", getLabel(id).getText());

        // Reset values to defaults
        $(TestBenchElement.class).id(id).$(TestBenchElement.class).id("button").click();

        Assert.assertEquals("default", getLabel(id).getText());
    }

    private WebElement getInput(String id) {
        return $(TestBenchElement.class).id(id).$(TestBenchElement.class).id("input");
    }

    private WebElement getLabel(String id) {
        return $(TestBenchElement.class).id(id).$(TestBenchElement.class).id("label");
    }
}
