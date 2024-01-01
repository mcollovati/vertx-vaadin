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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class VisibilityIT extends ChromeBrowserTest {

    @Test
    public void checkParentVisibility() {
        open();

        // The element is initially hidden. It shouldn't be bound.
        Assert.assertFalse(isElementPresent(By.id("visibility")));
        Assert.assertFalse(isElementPresent(By.id("nested-label")));

        WebElement main = findElement(By.id("main"));
        WebElement div = main.findElement(By.tagName("div"));

        // make the element visible
        WebElement visibilityButton = findElement(By.id("updateVisibiity"));
        scrollIntoViewAndClick(visibilityButton);

        Assert.assertNull(div.getAttribute("hidden"));
        Assert.assertEquals("visibility", div.getAttribute("id"));

        WebElement label = findElement(By.id("nested-label"));
        Assert.assertNull(label.getAttribute("hidden"));

        // change some properties for the element itself and it's child
        findElement(By.id("updateProperty")).click();

        Assert.assertEquals("foo", div.getAttribute("class"));
        Assert.assertEquals("bar", label.getAttribute("class"));

        // switch the visibility of the parent off and on
        scrollIntoViewAndClick(visibilityButton);

        Assert.assertEquals(Boolean.TRUE.toString(), div.getAttribute("hidden"));

        scrollIntoViewAndClick(visibilityButton);

        Assert.assertNull(label.getAttribute("hidden"));
    }

    @Test
    public void checkChildVisibility() {
        open();

        WebElement visibilityButton = findElement(By.id("updateLabelVisibiity"));
        scrollIntoViewAndClick(visibilityButton);

        // The element is initially hidden. It shouldn't be bound.
        Assert.assertFalse(isElementPresent(By.id("visibility")));
        Assert.assertFalse(isElementPresent(By.id("nested-label")));

        // make the parent visible
        findElement(By.id("updateVisibiity")).click();

        // now the child element is not bound, so it's invisible
        Assert.assertFalse(isElementPresent(By.id("nested-label")));

        // change some properties for child while it's invisible
        findElement(By.id("updateProperty")).click();

        // The element is still unbound and can't be found
        Assert.assertFalse(isElementPresent(By.id("nested-label")));

        // make it visible now
        scrollIntoViewAndClick(visibilityButton);

        WebElement label = findElement(By.id("nested-label"));
        Assert.assertNull(label.getAttribute("hidden"));

        Assert.assertEquals("bar", label.getAttribute("class"));

        // make it invisible
        scrollIntoViewAndClick(visibilityButton);

        Assert.assertEquals(Boolean.TRUE.toString(), label.getAttribute("hidden"));
    }
}
