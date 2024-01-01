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
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for handling internal errors and session expiration
 *
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class InternalErrorIT extends ChromeBrowserTest {

    private static final String UPDATE = "update";
    private static final String CLOSE_SESSION = "close-session";
    private int count;

    @Override
    public void setup() throws Exception {
        super.setup();

        open();
        // make sure system message provider is resets
        clickButton("reset-system-messages");
    }

    @Test
    public void sessionExpired_refreshByDefault() {
        // Put a flag in the current window
        executeScript("window.foo = true");
        assertTrue((boolean) executeScript("return !!window.foo;"));

        // Click on a button that should update the UI        clickButton(UPDATE);
        clickButton(UPDATE);
        waitUntil(driver -> isMessageUpdated());

        // Just click on any button to make a request after killing the session
        clickButton(CLOSE_SESSION);

        // Wait until the UI does not have the updated message
        waitUntil(driver -> !isMessageUpdated());

        // window has been reloaded, thus, the flag must not be
        // in the new window
        assertFalse((boolean) executeScript("return !!window.foo;"));

        // Check that there is no notification about session expired
        assertFalse(
                "By default, the 'Session Expired' notification " + "should not be used",
                isSessionExpiredNotificationPresent());
    }

    @Test
    public void enableSessionExpiredNotification_sessionExpired_notificationShown() {
        clickButton("enable-notification");

        // Refresh to take the new config into use
        getDriver().navigate().refresh();

        clickButton(UPDATE);
        clickButton(CLOSE_SESSION);

        // Just click on any button to make a request after killing the session
        clickButton(CLOSE_SESSION);

        assertTrue(
                "After enabling the 'Session Expired' notification, "
                        + "the page should not be refreshed "
                        + "after killing the session",
                isMessageUpdated());
        assertTrue(
                "After enabling the 'Session Expired' notification "
                        + "and killing the session, the notification should be displayed",
                isSessionExpiredNotificationPresent());
    }

    @Test
    public void internalError_showNotification_clickNotification_refresh() {
        clickButton(UPDATE);

        clickButton("cause-exception");

        assertTrue(
                "The page should not be immediately refreshed after " + "a server-side exception", isMessageUpdated());
        assertTrue(
                "'Internal error' notification should be present after " + "a server-side exception",
                isInternalErrorNotificationPresent());

        getErrorNotification().click();
        try {
            waitUntil(driver -> !isMessageUpdated());
        } catch (TimeoutException e) {
            Assert.fail("After internal error, clicking the notification "
                    + "should refresh the page, resetting the state of the UI.");
        }
        assertFalse(
                "'Internal error' notification should be gone after refreshing", isInternalErrorNotificationPresent());
    }

    @Test
    public void internalError_showNotification_clickEsc_refresh() {
        clickButton(UPDATE);

        clickButton("cause-exception");

        assertTrue(
                "The page should not be immediately refreshed after " + "a server-side exception", isMessageUpdated());
        assertTrue(
                "'Internal error' notification should be present after " + "a server-side exception",
                isInternalErrorNotificationPresent());

        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();
        try {
            waitUntil(driver -> !isMessageUpdated());
        } catch (TimeoutException e) {
            Assert.fail("After internal error, pressing esc-key should refresh the page, "
                    + "resetting the state of the UI.");
        }
        assertFalse(
                "'Internal error' notification should be gone after refreshing", isInternalErrorNotificationPresent());
    }

    @After
    public void resetSystemMessages() {
        waitUntil(ExpectedConditions.presenceOfElementLocated(By.id("reset-system-messages")));
        clickButton("reset-system-messages");
    }

    private boolean isMessageUpdated() {
        return "Updated".equals(findElement(By.cssSelector("div#message")).getText());
    }

    private boolean isSessionExpiredNotificationPresent() {
        return isErrorNotificationPresent("Session Expired");
    }

    private boolean isInternalErrorNotificationPresent() {
        return isErrorNotificationPresent("Internal error");
    }

    private boolean isErrorNotificationPresent(String text) {
        if (!isElementPresent(By.className("v-system-error"))) {
            return false;
        }
        return getErrorNotification().getAttribute("innerHTML").contains(text);
    }

    private WebElement getErrorNotification() {
        return findElement(By.className("v-system-error"));
    }

    private void clickButton(String id) {
        findElement(By.id(id)).click();
    }
}
