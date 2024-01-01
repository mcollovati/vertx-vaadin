/*
 * The MIT License
 * Copyright © 2000-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui.routing;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class PushRouteNotFoundIT extends ChromeBrowserTest {

    @Test
    @Ignore("Push is no more configurable per view")
    public void renderRouteNotFoundErrorPage_pushIsSpecifiedViaParentLayout() {
        open();

        waitUntil(driver -> isElementPresent(By.cssSelector("#push-layout #push-mode")));
        TestBenchElement push = $(TestBenchElement.class)
                .id("push-layout")
                .$(TestBenchElement.class)
                .id("push-mode");
        Assert.assertEquals("Push mode: AUTOMATIC", push.getText());
    }

    @Test
    public void renderRouteNotFoundErrorPage_parentLayoutReroute_reroutingIsDone() {
        String url = getTestURL(getRootURL(), doGetTestPath(PushLayout.FORWARD_PATH), new String[0]);

        getDriver().get(url);

        waitUntil(driver -> driver.getCurrentUrl().endsWith(ForwardPage.class.getName()));

        Assert.assertTrue(isElementPresent(By.id("forwarded")));
    }

    @Override
    protected String getTestPath() {
        return doGetTestPath(PushRouteNotFoundView.PUSH_NON_EXISTENT_PATH);
    }

    private String doGetTestPath(String uri) {
        String path = super.getTestPath();
        int index = path.lastIndexOf("/");
        return path.substring(0, index + 1) + uri;
    }
}
