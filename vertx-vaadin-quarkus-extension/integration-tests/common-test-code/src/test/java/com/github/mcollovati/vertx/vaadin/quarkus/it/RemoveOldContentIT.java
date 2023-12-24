package com.github.mcollovati.vertx.vaadin.quarkus.it;

import java.util.List;

import com.github.mcollovati.vertx.vaadin.quarkus.it.regression.RemoveOldContentView;
import com.vaadin.testbench.TestBenchElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@QuarkusIntegrationTest
public class RemoveOldContentIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/first-child-route";
    }

    @Test
    public void removeUIScopedRouterLayoutContent_navigateToAnotherRouteInsideMainLayoutAndBack_subLayoutOldContentRemoved() {
        open();
        waitForElementPresent(By.id(
                RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(
                RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(RemoveOldContentView.SUB_LAYOUT_ID));

        assertSubLayoutHasNoOldContent();
    }

    @Test
    public void removeUIScopedRouterLayoutContent_navigateToAnotherRouteOutsideMainLayoutAndBack_mainLayoutOldContentRemoved() {
        open();
        waitForElementPresent(By.id(
                RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(
                RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(RemoveOldContentView.SUB_LAYOUT_ID));

        assertSubLayoutHasNoOldContent();
    }

    private void assertSubLayoutHasNoOldContent() {
        TestBenchElement subLayout = $("div")
                .id(RemoveOldContentView.SUB_LAYOUT_ID);
        List<WebElement> subLayoutChildren = subLayout
                .findElements(By.tagName("div"));
        Assertions.assertEquals(1, subLayoutChildren.size());
    }
}
