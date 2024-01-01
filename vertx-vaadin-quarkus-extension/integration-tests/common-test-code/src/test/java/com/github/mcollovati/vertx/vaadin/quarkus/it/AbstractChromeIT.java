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
package com.github.mcollovati.vertx.vaadin.quarkus.it;

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.annotations.BrowserConfiguration;
import com.vaadin.testbench.parallel.Browser;
import com.vaadin.testbench.parallel.BrowserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Simplified chrome test that doesn't handle view/IT class paths. Uses Jupiter
 * API
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(ScreenshotsOnFailureExtension.class)
public abstract class AbstractChromeIT extends ChromeBrowserTest {

    @AfterEach
    public void tearDown() {
        getDriver().quit();
    }

    @BeforeAll
    public void setCapabilities() {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
    }

    @BeforeEach
    public void beforeTest() throws Exception {
        setup();
        waitUntil(driver -> {
            try {
                checkIfServerAvailable();
                return true;
            } catch (IllegalStateException ex) {
                // server not yet ready
            }
            return false;
        });
    }

    @Override
    protected void updateHeadlessChromeOptions(ChromeOptions chromeOptions) {
        chromeOptions.addArguments("--remote-allow-origins=*");
    }

    @Override
    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowsersToTest() {
        return Arrays.asList(BrowserUtil.chrome());
    }
}
