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

import java.util.List;

import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class NavigationTriggerIT extends ChromeBrowserTest {
    @Test
    public void testNavigationTriggers() {
        String url = getTestURL() + "/abc/";
        getDriver().get(url);

        assertMessageCount(1);

        assertLastMessage(
                "/abc", isClientRouter() ? NavigationTrigger.CLIENT_SIDE : NavigationTrigger.PAGE_LOAD, "abc");
        Assert.assertEquals(
                "The trailing '/' from the URL should be removed.",
                url.substring(0, url.length() - 1),
                getDriver().getCurrentUrl());

        findElement(By.id("routerlink")).click();
        assertMessageCount(2);
        assertLastMessage(
                "/routerlink",
                isClientRouter() ? NavigationTrigger.CLIENT_SIDE : NavigationTrigger.ROUTER_LINK,
                "routerlink");

        findElement(By.id("navigate")).click();
        assertMessageCount(3);
        assertLastMessage("/navigate", NavigationTrigger.UI_NAVIGATE, "navigate");
    }

    @Test
    @Ignore("Ignored because of fusion issue https://github.com/vaadin/flow/issues/10513")
    public void testNavigationTriggers_back_forward() {
        String url = getTestURL() + "/abc/";
        getDriver().get(url);

        findElement(By.id("routerlink")).click();

        findElement(By.id("navigate")).click();

        getDriver().navigate().back();
        assertMessageCount(4);
        assertLastMessage(
                "/routerlink",
                isClientRouter() ? NavigationTrigger.CLIENT_SIDE : NavigationTrigger.HISTORY,
                "routerlink");

        getDriver().navigate().forward();
        assertMessageCount(5);
        assertLastMessage(
                "/navigate", isClientRouter() ? NavigationTrigger.CLIENT_SIDE : NavigationTrigger.HISTORY, "navigate");

        findElement(By.id("forwardButton")).click();
        assertMessageCount(6);
        assertLastMessage("/forwarded", NavigationTrigger.PROGRAMMATIC, "forwarded");

        findElement(By.id("rerouteButton")).click();
        assertMessageCount(7);
        assertLastMessage("/", NavigationTrigger.PROGRAMMATIC, "rerouted");
    }

    private void assertLastMessage(String path, NavigationTrigger trigger, String parameter) {
        List<WebElement> messages = getMessages();
        String lastMessageText = messages.get(messages.size() - 1).getText();

        Assert.assertEquals(NavigationTriggerView.buildMessage(path, trigger, parameter), lastMessageText);
    }

    private void assertMessageCount(int count) {
        Assert.assertEquals(count, getMessages().size());
    }

    private List<WebElement> getMessages() {
        return findElements(By.cssSelector(".message"));
    }
}
