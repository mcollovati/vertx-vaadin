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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class PolymerPropertyMutationInObserverIT extends ChromeBrowserTest {

    @Test
    public void property_mutation_inside_observers_synced_correctly() {
        open();

        List<WebElement> modelValueDivs = findElements(By.className("model-value"));
        Assert.assertEquals("Value changed twice initially", 2, modelValueDivs.size());
        Assert.assertEquals(
                "First value change should equal the initially set server side value",
                "Event old value: null, event value: initially set value, current model value: initially set value",
                modelValueDivs.get(0).getText());
        Assert.assertEquals(
                "Observer mutation has been transmitted to the server",
                "Event old value: initially set value, event value: mutated, current model value: mutated",
                modelValueDivs.get(1).getText());

        TestBenchElement template = $(TestBenchElement.class).id("template");
        template.$(TestBenchElement.class).id("input").sendKeys(Keys.BACK_SPACE);

        modelValueDivs = findElements(By.className("model-value"));
        Assert.assertEquals("Value changed 4 times in total after backspace", 4, modelValueDivs.size());
        Assert.assertEquals(
                "User action mutation synced to server",
                "Event old value: mutated, event value: mutate, current model value: mutated",
                modelValueDivs.get(2).getText());
        Assert.assertEquals(
                "Observer mutation acting on the user action mutation synced to server",
                "Event old value: mutate, event value: mutated, current model value: mutated",
                modelValueDivs.get(3).getText());
    }
}
