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

public class LoadingIndicatorIT extends ChromeBrowserTest {

    @Test
    public void ensureSecondStyleWorks() throws InterruptedException {
        open("first=100", "second=1000", "third=100000");
        WebElement loadingIndicator = findElement(By.className("v-loading-indicator"));
        testBench().disableWaitForVaadin();
        findElement(By.id("wait5000")).click();
        Assert.assertFalse(hasCssClass(loadingIndicator, "second"));
        Thread.sleep(2000);
        Assert.assertTrue(hasCssClass(loadingIndicator, "second"));
    }

    @Test
    public void byDefault_loadingIndicator_usesDefaultTheme() throws InterruptedException {
        open();

        WebElement loadingIndicator = findElement(By.className("v-loading-indicator"));

        Assert.assertEquals(
                "Default loading indicator theming should be applied", "4px", loadingIndicator.getCssValue("height"));

        // if the next part of the test gets unstable in some environment, just
        // delete it
        testBench().disableWaitForVaadin();
        findElement(By.id("wait10000")).click();

        Thread.sleep(6000);

        // during third stage (wait) the height is bumped to 7px
        Assert.assertEquals(
                "Default loading indicator theming is not applied", "7px", loadingIndicator.getCssValue("height"));
    }

    @Test
    public void loadingIndicator_switchingToCustomTheme_noDefaultThemeApplied() throws InterruptedException {
        open();

        WebElement loadingIndicator = findElement(By.className("v-loading-indicator"));

        // Check that default theme is applied
        Assert.assertEquals(
                "Default loading indicator theming should be applied", "4px", loadingIndicator.getCssValue("height"));
        int count = findElements(By.cssSelector("head > style")).size();

        // Removes the style tag with default theme from the client
        findElement(By.id("disable-theme")).click();

        // Check that one style tag was removed from head
        Assert.assertEquals(
                "One style tag should be removed",
                1,
                count - findElements(By.cssSelector("head > style")).size());
        // Check that default theme is not being applied
        Assert.assertEquals(
                "Default loading indicator theming should not be applied",
                "auto",
                loadingIndicator.getCssValue("height"));
    }
}
