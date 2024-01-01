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

/**
 * This testing is using a mock of browser html5 dnd support just for fun
 * because the headless chrome doesn't support html5 dnd:
 * https://bugs.chromium.org/p/chromedriver/issues/detail?id=2695
 */
public class DnDIT extends ChromeBrowserTest {

    @Test
    public void testCopyEffectElement_droppedToAllLanes() {
        open();

        TestBenchElement boxElement = getBoxElement("COPY");

        // not testing with the lane that does not have drop effect set, because
        // that just causes complex mocking logic and doesn't validate
        // anything else
        dragBoxToLanes(boxElement, getLaneElement("COPY"), true);
        dragBoxToLanes(boxElement, getLaneElement("MOVE"), false);
        dragBoxToLanes(boxElement, getLaneElement("LINK"), false);
        dragBoxToLanes(boxElement, getLaneElement("NONE"), false);
    }

    @Test
    public void testCopyEffectElement_droppedToDeactivatedLane_noDrop() {
        open();
        TestBenchElement boxElement = getBoxElement("COPY");

        dragBoxToLanes(boxElement, getLaneElement("COPY"), true);
        dragBoxToLanes(boxElement, getLaneElement("deactivated"), false);
    }

    private void dragBoxToLanes(TestBenchElement boxElement, TestBenchElement laneElement, boolean dropShouldOccur) {
        clearEvents();

        dragAndDrop(boxElement, laneElement);

        // need to wait for roundtrip, there should always be 2 events after dnd
        waitForElementPresent(By.id("event-" + (dropShouldOccur ? "3" : "2")));

        verifyStartEvent(1, boxElement);
        if (dropShouldOccur) {
            verifyDropEvent(2, boxElement, laneElement);
            verifyEndEvent(3, boxElement, laneElement);
        } else {
            verifyEndEvent(2, boxElement, null);
        }
    }

    private void verifyStartEvent(int i, TestBenchElement boxElement) {
        TestBenchElement eventlog = getEventlog(i);
        String expected = new StringBuilder()
                .append(i)
                .append(": Start: ")
                .append(boxElement.getText())
                .toString();
        Assert.assertEquals("Invalid start event details", expected, eventlog.getText());
    }

    private void verifyEndEvent(int i, TestBenchElement boxElement, TestBenchElement laneElement) {
        TestBenchElement eventlog = getEventlog(i);

        // dnd-simulation must hardcode replace a working drop effect when
        // nothing set. in reality, browser determines it based on effect
        // allowed, copy is the default if both are missing (Chrome)
        String dropEffect = laneElement == null ? "NONE" : laneElement.getText();

        String expected = new StringBuilder()
                .append(i)
                .append(": End: ")
                .append(boxElement.getText())
                .append(" ")
                .append(dropEffect)
                .toString();
        Assert.assertEquals("Invalid end event details", expected, eventlog.getText());
    }

    private void verifyDropEvent(int i, TestBenchElement boxElement, TestBenchElement laneElement) {
        TestBenchElement eventlog = getEventlog(i);

        String effectAllowed = boxElement.getText();
        String dropEffect = laneElement.getText();

        String expected = new StringBuilder()
                .append(i)
                .append(": Drop: ")
                .append(effectAllowed)
                .append(" ")
                .append(dropEffect)
                .toString();
        Assert.assertEquals("Invalid drop event details", expected, eventlog.getText());
    }

    private TestBenchElement getEventlog(int i) {
        return $(TestBenchElement.class).id("event-" + i);
    }

    private void clearEvents() {
        findElement(By.tagName("button")).click();
    }

    private TestBenchElement getLaneElement(String dropEffect) {
        return $(TestBenchElement.class).id("lane-" + dropEffect);
    }

    private TestBenchElement getBoxElement(String effectAllowed) {
        return $(TestBenchElement.class).id("box-" + effectAllowed);
    }
}
