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
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class InvisibleDefaultPropertyValueIT extends ChromeBrowserTest {

    @Ignore("https://github.com/vaadin/flow/issues/7356 "
            + "Worked due to a side effect that was removed in 3.0 due to not all synchronized "
            + "properties being updated for all sync-events. Also related (but not same): "
            + "https://github.com/vaadin/flow/issues/3556")
    @Test
    public void clientDefaultPropertyValues_invisibleElement_propertiesAreNotSent() {
        open();

        // template is initially invisible
        TestBenchElement template = $("default-property").first();
        Assert.assertEquals(Boolean.TRUE.toString(), template.getAttribute("hidden"));

        // The element is not bound -> not value for "text" property
        WebElement text = template.$(TestBenchElement.class).id("text");
        Assert.assertEquals("", text.getText());

        // "message" property has default cleint side value
        WebElement message = template.$(TestBenchElement.class).id("message");
        Assert.assertEquals("msg", message.getText());

        // Show email value which has default property defined on the client
        // side
        WebElement showEmail = $(TestBenchElement.class).id("show-email");
        showEmail.click();

        WebElement emailValue = $(TestBenchElement.class).id("email-value");
        // default property is not sent to the server side
        Assert.assertEquals("", emailValue.getText());

        // make the element visible
        WebElement button = $(TestBenchElement.class).id("set-visible");
        button.click();

        // properties that has server side values are updated
        Assert.assertEquals("foo", text.getText());

        WebElement name = template.$(TestBenchElement.class).id("name");
        Assert.assertEquals("bar", name.getText());

        Assert.assertEquals("updated-message", message.getText());

        WebElement email = template.$(TestBenchElement.class).id("email");
        Assert.assertEquals("foo@example.com", email.getText());

        // Now check the email value on the server side
        showEmail = $(TestBenchElement.class).id("show-email");
        showEmail.click();
        Assert.assertEquals("foo@example.com", emailValue.getText());
    }
}
