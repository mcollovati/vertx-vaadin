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

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SubPropertyModelIT extends ChromeBrowserTest {

    @Test
    public void subproperties() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        Assert.assertEquals(
                "message", template.$(TestBenchElement.class).id("msg").getText());

        template.$(TestBenchElement.class).id("button").click();

        Assert.assertEquals(
                "Updated", template.$(TestBenchElement.class).id("msg").getText());

        template.$(TestBenchElement.class).id("sync").click();

        WebElement syncedReport = findElement(By.id("synced-msg"));
        Assert.assertEquals("Set from the client", syncedReport.getText());

        TestBenchElement input = template.$(TestBenchElement.class).id("input");
        input.clear();
        input.sendKeys("foo");

        List<WebElement> valueUpdate = findElements(By.id("value-update"));
        Optional<WebElement> result = valueUpdate.stream()
                .filter(element -> element.getText().equals("foo"))
                .findAny();
        Assert.assertTrue(
                "Unable to find updated input value element. "
                        + "Looks like input hasn't sent an event for subproperty",
                result.isPresent());

        // click message
        template.$(TestBenchElement.class).id("msg").click();

        Assert.assertEquals(
                "Clicking status message did not get the same modelData as in the message box.",
                template.$(TestBenchElement.class).id("msg").getText(),
                findElement(By.id("statusClick")).getText());
    }
}
