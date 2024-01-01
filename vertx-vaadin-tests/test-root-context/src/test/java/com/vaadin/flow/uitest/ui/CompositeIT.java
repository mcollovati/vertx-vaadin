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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CompositeIT extends ChromeBrowserTest {

    @Test
    public void changeOnClient() {
        open();
        WebElement name = findElement(By.id(CompositeNestedView.NAME_ID));
        InputTextElement input = $(InputTextElement.class).id(CompositeNestedView.NAME_FIELD_ID);
        Assert.assertEquals("Name on server:", name.getText());
        input.setValue("123");
        Assert.assertEquals("Name on server: 123", name.getText());

        InputTextElement serverValueInput = $(InputTextElement.class).id(CompositeView.SERVER_INPUT_ID);
        WebElement serverValueButton = findElement(By.id(CompositeView.SERVER_INPUT_BUTTON_ID));

        serverValueInput.setValue("server value");
        serverValueButton.click();

        Assert.assertEquals("Name on server: server value", name.getText());
    }

    @Test
    public void htmlImportOfContentLoaded() {
        open();
        waitForElementPresent(By.id(CompositeView.COMPOSITE_PAPER_SLIDER));
        TestBenchElement paperSlider = (TestBenchElement) findElement(By.id(CompositeView.COMPOSITE_PAPER_SLIDER));
        Assert.assertEquals("100", paperSlider.getPropertyString("max"));
    }
}
