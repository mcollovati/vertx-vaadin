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

public class LongPollingPushIT extends ChromeBrowserTest {

    @Test
    public void openPage_thereAreNoErrorsInTheConsole() {
        open();
        checkLogsForErrors(msg -> msg.contains("sockjs-node") || msg.contains("[WDS] Disconnected!"));

        waitForElementNotPresent(By.cssSelector("span#child"));

        WebElement button = findElement(By.id("visibility"));

        button.click();

        waitForElementPresent(By.cssSelector("span#child"));
        WebElement span = findElement(By.cssSelector("span#child"));

        Assert.assertEquals("Some text", span.getAttribute("innerHTML"));
        /*
         * SockJS client may try to connect to sockjs node server:
         * https://github.com/sockjs/sockjs-node/blob/master/README.md.
         *
         * This entry may be ignored.
         */
        checkLogsForErrors(msg -> msg.contains("sockjs-node") || msg.contains("[WDS] Disconnected!"));
    }
}
