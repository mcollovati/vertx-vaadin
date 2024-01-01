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
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

public class EventHandlerIT extends ChromeBrowserTest {

    @Test
    public void handleEventOnServer() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        template.$(TestBenchElement.class).id("handle").click();
        Assert.assertTrue(
                "Unable to find server event handler invocation confirmation. "
                        + "Looks like 'click' event handler has not been invoked on the server side",
                isElementPresent(By.id("event-handler-result")));

        template.$(TestBenchElement.class).id("send").click();
        TestBenchElement container = $(TestBenchElement.class).id("event-data");
        List<TestBenchElement> divs = container.$("div").all();

        Assert.assertEquals(
                "Unexpected 'button' event data in the received event handler parameter",
                "button: 0",
                divs.get(1).getText());
        Assert.assertEquals(
                "Unexpected 'type' event data in the received event handler parameter",
                "type: click",
                divs.get(2).getText());
        Assert.assertEquals(
                "Unexpected 'tag' event data in the received event handler parameter",
                "tag: button",
                divs.get(3).getText());

        // Check event functionality for event with both client and server
        // handler
        template.$(TestBenchElement.class).id("overridden").click();

        Assert.assertTrue(
                "Unable to find server event handler invocation confirmation.",
                isElementPresent(By.id("overridden-event-handler-result")));

        Assert.assertEquals(
                "Received result wasn't updated by client!",
                "Overridden server event was invoked with result: ClientSide handler",
                findElement(By.id("overridden-event-handler-result")).getText());
    }
}
