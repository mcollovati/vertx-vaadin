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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class UpdatableModelPropertiesIT extends ChromeBrowserTest {

    @Test
    public void updateName_propertyIsSentToServer() {
        open();

        WebElement name = getElement("name");
        name.click();

        assertUpdate("foo");
    }

    @Test
    public void updateAge_propertyIsNotSentToServerIfIsNotSynced_propertyIsSentWhenSynced() {
        open();

        WebElement age = getElement("age");
        age.click();

        String value = age.getText();

        assertNoUpdate(value);

        getElement("syncAge").click();

        age.click();

        value = age.getText();
        assertUpdate(value);
    }

    @Test
    public void updateEmail_propertyIsSentToServer() {
        open();

        WebElement email = getElement("email");
        email.click();

        assertUpdate(email.getText());
    }

    @Test
    public void updateText_propertyIsNotSentToServer() {
        open();

        WebElement text = getElement("text");
        text.click();

        String value = text.getText();

        assertNoUpdate(value);
    }

    private WebElement getElement(String id) {
        TestBenchElement template = $(TestBenchElement.class).id("template");
        return template.$(TestBenchElement.class).id(id);
    }

    private void waitUpdate() {
        waitUntil(driver -> getElement("updateStatus").getText().startsWith("Update Done"));
    }

    private void assertUpdate(String expectedValue) {
        waitUpdate();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        WebElement value = template.$(TestBenchElement.class).id("property-value");
        Assert.assertEquals(expectedValue, value.getText());
    }

    private void assertNoUpdate(String unexpectedValue) {
        waitUpdate();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        WebElement value = template.$(TestBenchElement.class).id("property-value");
        Assert.assertNotEquals(unexpectedValue, value.getText());
    }
}
