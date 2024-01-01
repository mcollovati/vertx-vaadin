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

public class BasicTypeInListIT extends ChromeBrowserTest {

    @Test
    public void basicTypeInModeList() {
        open();
        TestBenchElement template = $(TestBenchElement.class).id("template");
        List<TestBenchElement> items =
                template.$(TestBenchElement.class).attribute("class", "item").all();

        Assert.assertEquals(2, items.size());
        Assert.assertEquals("foo", items.get(0).getText());
        Assert.assertEquals("bar", items.get(1).getText());

        findElement(By.id("add")).click();

        items = template.$(TestBenchElement.class).attribute("class", "item").all();

        Assert.assertEquals(3, items.size());
        Assert.assertEquals("newItem", items.get(2).getText());

        findElement(By.id("remove")).click();

        items = template.$(TestBenchElement.class).attribute("class", "item").all();

        Assert.assertEquals(2, items.size());
        Assert.assertEquals("bar", items.get(0).getText());
    }
}
