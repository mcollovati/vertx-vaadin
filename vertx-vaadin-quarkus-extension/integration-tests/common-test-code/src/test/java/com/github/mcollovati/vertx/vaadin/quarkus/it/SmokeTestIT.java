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

import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@QuarkusIntegrationTest
public class SmokeTestIT extends AbstractChromeIT {

    @Test
    public void smokeTest_clickButton() {
        open();
        checkLogs();
        waitForElementPresent(By.tagName("button"));
        final NativeButtonElement button = $(NativeButtonElement.class).first();
        Assertions.assertTrue(button.isDisplayed());

        button.click();

        Assertions.assertEquals(
                "hello quarkus CDI", $(LabelElement.class).first().getText());
    }

    @Test
    public void smokeTest_validateReusableTheme() {
        open();
        checkLogs();
        waitForElementPresent(By.tagName("button"));
        final WebElement element = findElement(By.className("centered-content"));

        Assertions.assertEquals("250px", element.getCssValue("max-width"), "Theme max-width was not applied.");
    }

    @Override
    protected String getTestPath() {
        return "/";
    }

    private void checkLogs() {
        checkLogsForErrors(msg ->
                msg.contains("webpack-internal:") && msg.contains("VaadinDevmodeGizmo") && msg.contains("Event"));
    }
}
