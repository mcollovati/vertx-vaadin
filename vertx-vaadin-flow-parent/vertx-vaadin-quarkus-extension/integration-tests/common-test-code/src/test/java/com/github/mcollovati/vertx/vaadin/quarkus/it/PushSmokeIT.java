package com.github.mcollovati.vertx.vaadin.quarkus.it;

import com.vaadin.testbench.TestBenchElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

@QuarkusIntegrationTest
class PushSmokeIT extends AbstractChromeIT {

    protected String getTestPath() {
        return "/push";
    }

    @Test
    void pushUpdatesEmbeddedWebComponent() {
        open();
        waitForElementPresent(By.id("push-update"));

        int expectedUpdates = 50;
        int initialUpdateCount = getUpdateCount();
        Assertions.assertTrue(initialUpdateCount < expectedUpdates,
                "The initial update count should be less than maximum 50, but it has value "
                        + initialUpdateCount);

        waitUntil(driver -> getUpdateCount() > initialUpdateCount, 10);

        int nextUpdateCount = getUpdateCount();

        Assertions.assertTrue(nextUpdateCount < expectedUpdates,
                "The next interim update count should be less than maximum 50, but it has value "
                        + nextUpdateCount);

        waitUntil(driver -> getUpdateCount() >= expectedUpdates
                && getUpdateCount() <= expectedUpdates + 1, 5);
    }

    private int getUpdateCount() {
        TestBenchElement div = $(TestBenchElement.class).id("push-update");
        String count = div.getText();
        return Integer.parseInt(count);
    }

}
