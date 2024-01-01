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
        Assertions.assertTrue(
                initialUpdateCount < expectedUpdates,
                "The initial update count should be less than maximum 50, but it has value " + initialUpdateCount);

        waitUntil(driver -> getUpdateCount() > initialUpdateCount, 10);

        int nextUpdateCount = getUpdateCount();

        Assertions.assertTrue(
                nextUpdateCount < expectedUpdates,
                "The next interim update count should be less than maximum 50, but it has value " + nextUpdateCount);

        waitUntil(driver -> getUpdateCount() >= expectedUpdates && getUpdateCount() <= expectedUpdates + 1, 5);
    }

    private int getUpdateCount() {
        TestBenchElement div = $(TestBenchElement.class).id("push-update");
        String count = div.getText();
        return Integer.parseInt(count);
    }
}
