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
package com.vaadin.flow.uitest.ui.template.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Normal tests with @Before are not implemented because each @Test starts new
 * Chrome process.
 */
public class ListBindingIT extends ChromeBrowserTest {

    @Test
    public void listDataBinding() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");

        checkInitialState(template);

        checkModelItemWorks(template);

        // Before running those methods list is set to ["1", "2", "3"]
        // see (ListBindingView.INITIAL_STATE)

        assertMethodWorksCorrectly("addElement", template, "1", "2", "3", "4");

        assertMethodWorksCorrectly("addElementByIndex", template, "4", "1", "2", "3");

        assertMethodWorksCorrectly("addNumerousElements", template, "1", "2", "3", "4", "5");

        assertMethodWorksCorrectly("addNumerousElementsByIndex", template, "4", "5", "1", "2", "3");

        assertMethodWorksCorrectly("clearList", template);

        assertMethodWorksCorrectly("removeSecondElementByIndex", template, "1", "3");

        assertMethodWorksCorrectly("removeFirstElementWithIterator", template, "2", "3");

        assertMethodWorksCorrectly("swapFirstAndSecond", template, "2", "1", "3");

        assertMethodWorksCorrectly("sortDescending", template, "3", "2", "1");

        assertMethodWorksCorrectly(
                "setInitialStateToEachMessage",
                template,
                ListBindingView.INITIAL_STATE,
                ListBindingView.INITIAL_STATE,
                ListBindingView.INITIAL_STATE);
    }

    private void checkModelItemWorks(TestBenchElement template) {
        resetState(template);

        List<TestBenchElement> msgs =
                template.$(TestBenchElement.class).attribute("class", "msg").all();

        // Click b message
        msgs.get(1).click();

        // Assert that the message was gotten correctly on the server side
        Assert.assertEquals(
                "Couldn't validate element click selection.",
                template.$(TestBenchElement.class).id("selection").getText(),
                "Clicked message: " + msgs.get(1).getText());
    }

    private void checkInitialState(TestBenchElement template) {
        Assert.assertEquals(Collections.singletonList(ListBindingView.INITIAL_STATE), getMessages(template));
    }

    private void assertMethodWorksCorrectly(String handlerName, TestBenchElement template, String... expectedMessages) {
        resetState(template);
        template.$(TestBenchElement.class).id(handlerName).click();

        Assert.assertEquals(Arrays.asList(expectedMessages), getMessages(template));
    }

    private void resetState(TestBenchElement template) {
        template.$(TestBenchElement.class).id("reset").click();
        Assert.assertEquals(ListBindingView.RESET_STATE, getMessages(template));
    }

    private List<String> getMessages(TestBenchElement template) {
        return template.$(TestBenchElement.class).attribute("class", "msg").all().stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
