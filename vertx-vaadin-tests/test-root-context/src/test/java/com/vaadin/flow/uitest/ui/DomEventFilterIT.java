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

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DomEventFilterIT extends ChromeBrowserTest {

    @Test
    public void filtering() {
        open();

        WebElement input = findElement(By.id("space"));

        input.sendKeys("asdf");

        Assert.assertEquals(0, getMessages().size());

        input.sendKeys("foo  bar");

        Assert.assertEquals(2, getMessages().size());
    }

    @Test
    public void debounce() throws InterruptedException {
        open();

        WebElement input = findElement(By.id("debounce"));

        input.sendKeys("a");
        assertMessages(0, "Leading: a", "Throttle: a");

        // Halfway into the idle interval
        Thread.sleep(500);
        input.sendKeys("b");

        /*
         * Wait until t = 1250: halfway between t = 1000 when the throttle
         * triggers and t = 1500 when the idle triggers
         */
        Thread.sleep(750);

        assertMessages(2, "Throttle: ab");

        /*
         * Wait until t = 1750: halfway between t = 1500 when the idle triggers
         * and t = 2000 when a new throttle would trigger
         */
        Thread.sleep(500);
        assertMessages(3, "Trailing: ab");

        /*
         * Wait until t = 2250: 250 beyond 2000 when a new throttle would
         * trigger if the idle hadn't triggered
         */
        Thread.sleep(500);
        assertMessages(4);
    }

    @Test
    public void componentWithDebounce() throws InterruptedException {
        open();

        WebElement input = findElement(By.id("debounce-component"));

        input.sendKeys("a");
        assertMessages(0);

        Thread.sleep(750);
        input.sendKeys("b");
        assertMessages(0);

        Thread.sleep(1100);
        assertMessages(0, "Component: ab");

        input.sendKeys("c");
        Thread.sleep(200);
        assertMessages(1);

        input.sendKeys("d");
        Thread.sleep(800);
        assertMessages(1);
        Thread.sleep(300);
        assertMessages(1, "Component: abcd");
    }

    @Test
    public void twoListeners_removingOne_should_cleanItsFilter() {
        open();

        WebElement paragraph = findElement(By.id("result-paragraph"));
        WebElement button = findElement(By.id("listener-removal-button"));
        WebElement input = findElement(By.id("listener-input"));

        Assert.assertEquals("Result paragraph should be empty", "", paragraph.getText());

        input.sendKeys("a");
        Assert.assertEquals(
                "Filter should have prevented default, and input is empty", "", input.getAttribute("value"));
        Assert.assertEquals("Event was sent to server and paragraph should be 'A'", "A", paragraph.getText());

        input.sendKeys("b");
        Assert.assertEquals(
                "Filter should have prevented default, and input is empty", "", input.getAttribute("value"));
        Assert.assertEquals("Event was sent to server and paragraph should be 'B'", "B", paragraph.getText());

        // remove keybind for A
        button.click();
        Assert.assertEquals("Result paragraph should be 'REMOVED'", "REMOVED", paragraph.getText());

        // keybind for A should no longer work
        input.sendKeys("a");
        Assert.assertEquals("Filter should be removed, and input has 'a'", "a", input.getAttribute("value"));
        Assert.assertEquals("Result paragraph should still be 'REMOVED'", "REMOVED", paragraph.getText());

        // b should still be functional
        input.sendKeys("b");
        Assert.assertEquals(
                "Filter should have prevented default, and input has only 'a'", "a", input.getAttribute("value"));
        Assert.assertEquals("Event was sent to server and paragraph should be 'B'", "B", paragraph.getText());
    }

    private void assertMessages(int skip, String... expectedTail) {
        List<WebElement> messages = getMessages();
        if (messages.size() < skip) {
            Assert.fail("Cannot skip " + skip + " messages when there are only " + messages.size() + "messages. "
                    + joinMessages(messages));
        }

        messages = messages.subList(skip, messages.size());

        if (messages.size() < expectedTail.length) {
            Assert.fail("Expected " + expectedTail.length
                    + " messages, but there are only " + messages.size() + ". "
                    + joinMessages(messages));
        }

        for (int i = 0; i < expectedTail.length; i++) {
            Assert.assertEquals(
                    "Unexpected message at index " + i,
                    expectedTail[i],
                    messages.get(i).getText());
        }

        if (messages.size() > expectedTail.length) {
            Assert.fail("There are unexpected messages at the end. "
                    + joinMessages(messages.subList(expectedTail.length, messages.size())));
        }
    }

    private static String joinMessages(List<WebElement> messages) {
        return messages.stream().map(WebElement::getText).collect(Collectors.joining("\n", "\n", ""));
    }

    private List<WebElement> getMessages() {
        WebElement messagesHolder = findElement(By.id("messages"));
        List<WebElement> messages = messagesHolder.findElements(By.cssSelector("div"));
        return messages;
    }
}
