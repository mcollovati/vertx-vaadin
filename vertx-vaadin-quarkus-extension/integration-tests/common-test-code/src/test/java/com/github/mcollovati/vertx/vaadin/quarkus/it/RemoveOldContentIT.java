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

import java.util.List;

import com.vaadin.testbench.TestBenchElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.github.mcollovati.vertx.vaadin.quarkus.it.regression.RemoveOldContentView;

@QuarkusIntegrationTest
public class RemoveOldContentIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/first-child-route";
    }

    @Test
    public void
            removeUIScopedRouterLayoutContent_navigateToAnotherRouteInsideMainLayoutAndBack_subLayoutOldContentRemoved() {
        open();
        waitForElementPresent(By.id(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(
                By.id(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(RemoveOldContentView.SUB_LAYOUT_ID));

        assertSubLayoutHasNoOldContent();
    }

    @Test
    public void
            removeUIScopedRouterLayoutContent_navigateToAnotherRouteOutsideMainLayoutAndBack_mainLayoutOldContentRemoved() {
        open();
        waitForElementPresent(By.id(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_TO_ANOTHER_ROUTE_OUTSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(
                By.id(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID));
        click(RemoveOldContentView.NAVIGATE_BACK_FROM_ANOTHER_ROUTE_INSIDE_MAIN_LAYOUT_BUTTON_ID);
        waitForElementPresent(By.id(RemoveOldContentView.SUB_LAYOUT_ID));

        assertSubLayoutHasNoOldContent();
    }

    private void assertSubLayoutHasNoOldContent() {
        TestBenchElement subLayout = $("div").id(RemoveOldContentView.SUB_LAYOUT_ID);
        List<WebElement> subLayoutChildren = subLayout.findElements(By.tagName("div"));
        Assertions.assertEquals(1, subLayoutChildren.size());
    }
}
