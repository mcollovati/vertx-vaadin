/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui.template;

import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class BeanInListingIT extends ChromeBrowserTest {

    private static final class SelectedCondition implements ExpectedCondition<Boolean> {

        private final WebElement selected;
        private final String expected;

        SelectedCondition(WebElement selected, String value) {
            this.selected = selected;
            expected = value;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            return selected.getText().equals(expected);
        }

        @Override
        public String toString() {
            return "The value of selected element is '" + selected.getText() + "', expected '" + expected + "'";
        }
    }

    @Test
    public void beanInTwoWayBinding() throws InterruptedException {
        open();

        TestBenchElement selected = $(TestBenchElement.class)
                .id("template")
                .$(TestBenchElement.class)
                .id("selected");

        assertSelectionValue("user-item", selected, "foo");

        assertSelectionValue("user-item", selected, "bar");

        assertSelectionValue("msg-item", selected, "baz");

        assertSelectionValue("msg-item", selected, "msg");
    }

    private void assertSelectionValue(String className, WebElement selected, String item) {
        TestBenchElement template = $(TestBenchElement.class).id("template");
        List<TestBenchElement> items =
                template.$(TestBenchElement.class).attribute("class", className).all();
        items.stream()
                .filter(itemElement -> itemElement.getText().equals(item))
                .findFirst()
                .get()
                .click();

        waitUntil(new SelectedCondition(selected, item));
    }
}
