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

public class EnabledIT extends ChromeBrowserTest {

    @Test
    public void verifyEnabledState() {
        open();

        // The element is initially disabled. It should be there.
        Assert.assertTrue(isElementPresent(By.id("enabled")));
        Assert.assertTrue(isElementPresent(By.id("nested-label")));

        // check that disabled update property button is present.
        Assert.assertTrue(isElementPresent(By.id("updateProperty")));
        // Validate that button has the default disabled attribute
        Assert.assertTrue(findElement(By.id("updateProperty")).getAttribute("disabled") != null);

        WebElement main = findElement(By.id("main"));
        WebElement div = main.findElement(By.tagName("div"));

        // try to change some properties for the element and it's child
        findElement(By.id("updateProperty")).click();

        WebElement label = findElement(By.id("nested-label"));

        // assert that no changes occurred due to disabled button.
        Assert.assertEquals("", div.getAttribute("class"));
        Assert.assertEquals("", label.getAttribute("class"));

        // make the button enabled
        WebElement enableButton = findElement(By.id("enableButton"));
        scrollIntoViewAndClick(enableButton);

        // change some properties for the element itself and it's child
        findElement(By.id("updateProperty")).click();

        // properties should have changed from the server
        Assert.assertEquals("foo", div.getAttribute("class"));
        Assert.assertEquals("bar", label.getAttribute("class"));
    }
}
