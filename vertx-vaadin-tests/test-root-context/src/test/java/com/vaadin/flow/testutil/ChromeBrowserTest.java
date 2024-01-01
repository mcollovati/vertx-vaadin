/*
 * The MIT License
 * Copyright © 2000-2022 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.testutil;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.testcategory.ChromeTests;
import com.vaadin.flow.testutil.net.PortProber;
import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.parallel.Browser;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for TestBench tests to run locally in the Chrome browser.
 * <p>
 * It is required to set system property with path to the driver to be able to
 * run the test.
 * <p>
 * The test can be executed locally and on a test Hub. Chrome browser is used
 * only if test is executed locally. The test Hub target browsers can be
 * configured via {@link #getHubBrowsersToTest()}.
 *
 *
 * @author Vaadin Ltd
 * @since 1.0
 *
 */
@Category(ChromeTests.class)
public class ChromeBrowserTest extends ViewOrUITest {

    private static InetAddress ipv4All;
    private static InetAddress ipv6All;

    static {
        try {
            ipv4All = InetAddress.getByName("0.0.0.0");
            ipv6All = InetAddress.getByName("::0");
        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Before
    @Override
    public void setup() throws Exception {
        if (Browser.CHROME == getRunLocallyBrowser() && !isJavaInDebugMode()) {
            setDriver(createHeadlessChromeDriver(this::updateHeadlessChromeOptions));
        } else {
            super.setup();
        }
    }

    /**
     * Allows modifying the chrome options to be used when running on a local
     * Chrome.
     *
     * @param chromeOptions
     *            chrome options to use when running on a local Chrome
     */
    protected void updateHeadlessChromeOptions(ChromeOptions chromeOptions) {}

    static boolean isJavaInDebugMode() {
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .toString()
                .contains("jdwp");
    }

    static WebDriver createHeadlessChromeDriver(Consumer<ChromeOptions> optionsUpdater) {
        for (int i = 0; i < 3; i++) {
            try {
                return tryCreateHeadlessChromeDriver(optionsUpdater);
            } catch (Exception e) {
                getLogger().warn("Unable to create chromedriver on attempt " + i, e);
            }
        }
        throw new RuntimeException("Gave up trying to create a chromedriver instance");
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(ChromeBrowserTest.class);
    }

    private static WebDriver tryCreateHeadlessChromeDriver(Consumer<ChromeOptions> optionsUpdater) {
        ChromeOptions headlessOptions = createHeadlessChromeOptions();
        optionsUpdater.accept(headlessOptions);

        int port = PortProber.findFreePort();
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingPort(port)
                .withSilent(true)
                .build();
        ChromeDriver chromeDriver = new ChromeDriver(service, headlessOptions);
        return TestBench.createDriver(chromeDriver);
    }

    @Override
    protected List<DesiredCapabilities> getHubBrowsersToTest() {
        return getBrowserCapabilities(Browser.CHROME);
    }

    @Override
    protected List<DesiredCapabilities> getBrowserCapabilities(Browser... browsers) {
        return customizeCapabilities(super.getBrowserCapabilities(browsers));
    }

    protected List<DesiredCapabilities> customizeCapabilities(List<DesiredCapabilities> capabilities) {

        capabilities.stream()
                .filter(cap -> "chrome".equalsIgnoreCase(cap.getBrowserName()))
                .forEach(cap -> cap.setCapability(ChromeOptions.CAPABILITY, createHeadlessChromeOptions()));

        return capabilities;
    }

    static ChromeOptions createHeadlessChromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--remote-allow-origins=*");
        return options;
    }
}
