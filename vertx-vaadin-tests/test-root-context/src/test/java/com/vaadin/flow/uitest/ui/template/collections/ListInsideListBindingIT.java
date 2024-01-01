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

import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 * Normal tests with @Before are not implemented because each @Test starts new
 * Chrome process.
 */
public class ListInsideListBindingIT extends ChromeBrowserTest {

    @Test
    public void listDataBinding() {
        int initialSize = 4;
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");
        checkMessagesRemoval(template, initialSize);
        template.$(TestBenchElement.class).id("reset").click();
        checkAllElementsUpdated(template, initialSize);
    }

    private void checkMessagesRemoval(TestBenchElement template, int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            List<TestBenchElement> currentMessages = template.$(TestBenchElement.class)
                    .attribute("class", "submsg")
                    .all();
            Assert.assertEquals("Wrong amount of nested messages", initialSize - i, currentMessages.size());

            WebElement messageToRemove = currentMessages.iterator().next();
            String messageToRemoveText = messageToRemove.getText();
            messageToRemove.click();

            String removedMessageLabelText =
                    template.$(TestBenchElement.class).id("removedMessage").getText();
            Assert.assertEquals(
                    "Expected removed message text to appear",
                    "Removed message: " + messageToRemoveText,
                    removedMessageLabelText);
        }
    }

    private void checkAllElementsUpdated(TestBenchElement template, int initialSize) {
        template.$(TestBenchElement.class).id("updateAllElements").click();
        List<TestBenchElement> msgs =
                template.$(TestBenchElement.class).attribute("class", "submsg").all();
        Assert.assertEquals("Wrong amount of nested messages", initialSize, msgs.size());
        msgs.forEach(msg ->
                Assert.assertEquals("Message was not updated", ListInsideListBindingView.UPDATED_TEXT, msg.getText()));
    }
}
