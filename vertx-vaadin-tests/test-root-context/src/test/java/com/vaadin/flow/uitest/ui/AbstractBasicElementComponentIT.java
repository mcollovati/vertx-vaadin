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
package com.vaadin.flow.uitest.ui;

import java.util.List;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class AbstractBasicElementComponentIT extends ChromeBrowserTest {

    @Test
    public void ensureDomUpdatesAndEventsDoSomething() {
        open();
        assertDomUpdatesAndEventsDoSomething();
    }

    protected void assertDomUpdatesAndEventsDoSomething() {
        Assert.assertEquals(0, getThankYouCount());
        $(InputTextElement.class).first().setValue("abc");

        // Need to call WebElement.click(), because TestBenchElement.click()
        // clicks with JS which doesn't move the mouse on IE11, and breaks the
        // mouse coordinate test.
        $(NativeButtonElement.class).first().getWrappedElement().click();

        Assert.assertEquals(1, getThankYouCount());

        String buttonText = getThankYouElements().get(0).getText();
        String expected = "Thank you for clicking \"Click me\" at \\((\\d+),(\\d+)\\)! The field value is abc";
        Assert.assertTrue("Expected '" + expected + "', was '" + buttonText + "'", buttonText.matches(expected));

        // Clicking removes the element
        getThankYouElements().get(0).click();

        Assert.assertEquals(0, getThankYouCount());

        WebElement helloElement = findElement(By.id("hello-world"));

        Assert.assertEquals("Hello world", helloElement.getText());
        Assert.assertEquals("hello", helloElement.getAttribute("class"));

        helloElement.click();
        Assert.assertEquals("Stop touching me!", helloElement.getText());
        Assert.assertEquals("", helloElement.getAttribute("class"));

        // Clicking again shouldn't have any effect
        helloElement.click();
        Assert.assertEquals("Stop touching me!", helloElement.getText());
    }

    protected int getThankYouCount() {
        return getThankYouElements().size();
    }

    protected List<WebElement> getThankYouElements() {
        return findElements(By.cssSelector(".thankYou"));
    }
}
