/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import java.util.ArrayList;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;

public class PageIT extends ChromeBrowserTest {

    @Test
    public void testPageTitleUpdates() {
        open();

        updateTitle("Page title 1");
        verifyTitle("Page title 1");

        updateTitle("FOObar");
        verifyTitle("FOObar");
    }

    @Test
    public void testOnlyMostRecentPageUpdate() {
        open();

        updateTitle("Page title 1");
        verifyTitle("Page title 1");

        findElement(By.cssSelector("input#input")).sendKeys("title 2" + Keys.TAB);
        findElement(By.id("override")).click();

        verifyTitle("OVERRIDDEN");
    }

    @Test
    public void testPageTitleClears() {
        open();

        findElement(By.id("override")).click();
        verifyTitle("OVERRIDDEN");

        updateTitle("");
        verifyTitle("");
    }

    private void verifyTitle(String title) {
        Assert.assertEquals("Page title does not match", title, getDriver().getTitle());
    }

    private void updateTitle(String title) {
        $(InputTextElement.class).id("input").setValue(title);
        findElement(By.cssSelector("div#button")).click();
    }

    @Test
    public void testReload() {
        open();

        InputTextElement input = $(InputTextElement.class).id("input");
        input.setValue("foo");
        Assert.assertEquals("foo", input.getPropertyString("value"));
        findElement(By.cssSelector("div#reload")).click();
        input = $(InputTextElement.class).id("input");
        Assert.assertEquals("", input.getValue());
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7575")
    public void testSetLocation() {
        open();

        findElement(By.id("setLocation")).click();
        Assert.assertThat(getDriver().getCurrentUrl(), Matchers.endsWith(BaseHrefView.class.getName()));
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7575")
    public void testOpenUrlInNewTab() {
        open();

        findElement(By.id("open")).click();
        ArrayList<String> tabs = new ArrayList<>(getDriver().getWindowHandles());
        Assert.assertThat(
                getDriver().switchTo().window(tabs.get(1)).getCurrentUrl(),
                Matchers.endsWith(BaseHrefView.class.getName()));
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7575")
    public void testOpenUrlInIFrame() throws InterruptedException {
        open();

        findElement(By.id("openInIFrame")).click();

        waitUntil(driver -> !getIframeUrl().equals("about:blank"));

        Assert.assertThat(getIframeUrl(), Matchers.endsWith(BaseHrefView.class.getName()));
    }

    private String getIframeUrl() {
        return (String) ((JavascriptExecutor) driver)
                .executeScript("return document.getElementById('newWindow').contentWindow.location.href;");
    }
}
