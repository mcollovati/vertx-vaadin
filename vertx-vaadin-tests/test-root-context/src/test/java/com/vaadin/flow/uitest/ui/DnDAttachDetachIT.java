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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.vaadin.flow.uitest.ui.DnDAttachDetachView.DRAGGABLE_ID;
import static com.vaadin.flow.uitest.ui.DnDAttachDetachView.MOVE_BUTTON_ID;
import static com.vaadin.flow.uitest.ui.DnDAttachDetachView.SWAP_BUTTON_ID;
import static com.vaadin.flow.uitest.ui.DnDAttachDetachView.VIEW_1_ID;
import static com.vaadin.flow.uitest.ui.DnDAttachDetachView.VIEW_2_ID;

public class DnDAttachDetachIT extends ChromeBrowserTest {

    /* https://github.com/vaadin/flow/issues/6054 */
    @Test
    public void testDnD_attachDetachAttachSourceAndTarget_dndOperationWorks() {
        open();

        dragAndDrop(getDraggableText(), getDropTarget());

        TestBenchElement eventElement = getEvent(0);
        Assert.assertEquals("Drop: 0", eventElement.getText());

        clickElementWithJs(SWAP_BUTTON_ID);

        // just verify that the component was removed
        waitForElementNotPresent(By.id(VIEW_1_ID));
        waitForElementPresent(By.id(VIEW_2_ID));

        clickElementWithJs(SWAP_BUTTON_ID);

        dragAndDrop(getDraggableText(), getDropTarget());

        // without proper reactivation of the drop target, the following event
        // is not discoved
        eventElement = getEvent(1);
        Assert.assertEquals("Drop: 1", eventElement.getText());

        Assert.assertFalse("No second event should have occurred", isElementPresent(By.id("drop-" + 2)));
    }

    @Test
    public void testDnD_moveComponents_dndOperationWorks() {
        open();

        dragAndDrop(getDraggableText(), getDropTarget());

        TestBenchElement eventElement = getEvent(0);
        Assert.assertEquals("Drop: 0", eventElement.getText());

        clickElementWithJs(MOVE_BUTTON_ID);

        dragAndDrop(getDraggableText(), getDropTarget());

        eventElement = getEvent(1);
        Assert.assertEquals("Drop: 1", eventElement.getText());

        Assert.assertFalse("No second event should have occurred", isElementPresent(By.id("drop-" + 2)));
    }

    private TestBenchElement getEvent(int i) {
        waitForElementPresent(By.id("drop-" + i));
        return $(TestBenchElement.class).id("drop-" + i);
    }

    private TestBenchElement getDraggableText() {
        return id(DRAGGABLE_ID);
    }

    private TestBenchElement getDropTarget() {
        return id(VIEW_1_ID);
    }

    private TestBenchElement id(String id) {
        return $(TestBenchElement.class).id(id);
    }
}
