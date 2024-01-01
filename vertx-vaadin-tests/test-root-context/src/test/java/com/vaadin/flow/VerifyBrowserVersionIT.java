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
package com.vaadin.flow;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class VerifyBrowserVersionIT extends ChromeBrowserTest {

    @Test
    public void verifyUserAgent() {
        open();

        DesiredCapabilities desiredCapabilities = getDesiredCapabilities();

        String userAgent = findElement(By.id("userAgent")).getText();
        String browserIdentifier;

        if (BrowserUtil.isChrome(getDesiredCapabilities())) {
            // Chrome version does not necessarily match the desired version
            // because of auto updates...
            browserIdentifier = getExpectedUserAgentString(getDesiredCapabilities())
                    + System.getProperty("uitest.chrome-version", "84");
        } else if (BrowserUtil.isFirefox(getDesiredCapabilities())) {
            browserIdentifier = getExpectedUserAgentString(getDesiredCapabilities())
                    + System.getProperty("uitest.firefox-version", "58");
        } else {
            browserIdentifier =
                    getExpectedUserAgentString(desiredCapabilities) + desiredCapabilities.getBrowserVersion();
        }

        assertThat(userAgent, containsString(browserIdentifier));
    }

    private String getExpectedUserAgentString(DesiredCapabilities dCap) {
        if (BrowserUtil.isFirefox(dCap)) {
            return "Firefox/";
        } else if (BrowserUtil.isChrome(dCap)) {
            return "Chrome/";
        }
        throw new UnsupportedOperationException("Test is being run on unknown browser.");
    }
}
