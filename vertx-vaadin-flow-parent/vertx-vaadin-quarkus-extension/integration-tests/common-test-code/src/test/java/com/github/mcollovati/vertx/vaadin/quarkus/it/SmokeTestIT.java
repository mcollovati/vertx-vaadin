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

        Assertions.assertEquals("hello quarkus CDI",
                $(LabelElement.class).first().getText());
    }

    @Test
    public void smokeTest_validateReusableTheme() {
        open();
        checkLogs();
        waitForElementPresent(By.tagName("button"));
        final WebElement element = findElement(
                By.className("centered-content"));

        Assertions.assertEquals("250px", element.getCssValue("max-width"),
                "Theme max-width was not applied.");
    }

    @Override
    protected String getTestPath() {
        return "/";
    }

    private void checkLogs() {
        checkLogsForErrors(msg -> msg.contains("webpack-internal:")
                && msg.contains("VaadinDevmodeGizmo") && msg.contains("Event"));
    }
}
