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
package com.vaadin.flow.uitest.ui.template.collections;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class ClearListIT extends ChromeBrowserTest {

    @Test
    public void checkThatListCanBeClearedWithModelHavingNoDefaultConstructor() {
        checkThatModelHasNoDefaultConstructor();
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        List<String> initialMessages = getMessages(template);

        Assert.assertEquals(
                "Initial page does not contain expected messages", Arrays.asList("1", "2"), initialMessages);

        template.$(TestBenchElement.class).id("clearList").click();

        Assert.assertTrue(
                "Page should not contain elements after we've cleared them",
                getMessages(template).isEmpty());
    }

    private void checkThatModelHasNoDefaultConstructor() {
        Constructor<?>[] modelConstructors = ClearListView.Message.class.getConstructors();
        Assert.assertEquals("Expect model to have one constructor exactly", 1, modelConstructors.length);
        Assert.assertTrue(
                "Expect model to have at least one parameter in its single constructor",
                modelConstructors[0].getParameterCount() > 0);
    }

    private List<String> getMessages(TestBenchElement template) {
        return template.$(TestBenchElement.class).attribute("class", "msg").all().stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
