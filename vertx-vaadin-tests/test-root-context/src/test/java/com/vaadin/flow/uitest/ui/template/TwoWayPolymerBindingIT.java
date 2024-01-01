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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class TwoWayPolymerBindingIT extends ChromeBrowserTest {

    @Test
    public void initialModelValueIsPresentAndModelUpdatesNormally() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        WebElement input = template.$(TestBenchElement.class).id("input");

        // The initial client-side value should be sent from the client to the
        // model
        waitUntil(driver -> "Value: foo".equals(getStatusMessage()));

        // now make explicit updates from the client side
        input.clear();
        input.sendKeys("a");
        Assert.assertEquals("Value: a", getStatusMessage());

        input.sendKeys("b");
        Assert.assertEquals("Value: ab", getStatusMessage());

        // Reset the model value from the server-side
        template.$(TestBenchElement.class).id("reset").click();
        Assert.assertEquals("Value:", getStatusMessage());
        Assert.assertEquals("", getValueProperty(input));

        input.sendKeys("c");
        Assert.assertEquals("Value: c", getStatusMessage());
    }

    private Object getValueProperty(WebElement input) {
        return ((JavascriptExecutor) getDriver()).executeScript("return arguments[0].value", input);
    }

    private String getStatusMessage() {
        TestBenchElement template = $(TestBenchElement.class).id("template");

        return template.$(TestBenchElement.class).id("status").getText();
    }
}
