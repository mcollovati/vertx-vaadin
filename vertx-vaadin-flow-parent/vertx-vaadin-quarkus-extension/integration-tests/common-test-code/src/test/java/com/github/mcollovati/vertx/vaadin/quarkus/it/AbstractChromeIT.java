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
        ChromeBrowserTest.setChromeDriverPath();
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
