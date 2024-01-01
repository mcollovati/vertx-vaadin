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
package com.vaadin.flow.uitest.ui.scroll;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MultipleAnchorsIT extends AbstractScrollIT {

    @Test
    public void numerousDifferentAnchorsShouldWorkAndHistoryShouldBePreserved() {
        testBench().resizeViewPortTo(700, 800);
        open();

        final Long initialHistoryLength = getBrowserHistoryLength();

        int anchorIndex = 0;
        while (anchorIndex < MultipleAnchorsView.NUMBER_OF_ANCHORS) {
            clickElementWithJs(MultipleAnchorsView.ANCHOR_URL_ID_BASE + anchorIndex);
            verifyAnchor(anchorIndex);
            anchorIndex++;
        }

        assertThat(
                "Browser history length should be increased by number of anchor urls="
                        + MultipleAnchorsView.NUMBER_OF_ANCHORS,
                getBrowserHistoryLength(),
                is(initialHistoryLength + MultipleAnchorsView.NUMBER_OF_ANCHORS));

        anchorIndex--;

        while (anchorIndex > 0) {
            anchorIndex--;
            driver.navigate().back();
            verifyAnchor(anchorIndex);
        }
    }

    @Test
    public void numerousEqualAnchorsShouldRepresentOneHistoryEntry() {
        testBench().resizeViewPortTo(700, 800);
        open();

        final Long initialHistoryLength = getBrowserHistoryLength();
        final String initialUrl = driver.getCurrentUrl();
        final int indexToClick = 2;

        for (int i = 0; i < 10; i++) {
            clickElementWithJs(MultipleAnchorsView.ANCHOR_URL_ID_BASE + indexToClick);
            verifyAnchor(indexToClick);
        }

        assertThat(
                "Browser history length should be increased by 1 (number of different anchor urls used)",
                getBrowserHistoryLength(),
                is(initialHistoryLength + 1));

        driver.navigate().back();
        assertThat("Expected to have initialUrl", driver.getCurrentUrl(), is(initialUrl));
        ensureThatNewPageIsNotScrolled();
    }

    private void verifyAnchor(int idNumber) {
        Point anchorElementLocation = findElement(By.id(MultipleAnchorsView.ANCHOR_DIV_ID_BASE + idNumber))
                .getLocation();
        assertThat(
                "Expected url to change to anchor one",
                driver.getCurrentUrl(),
                endsWith(MultipleAnchorsView.ANCHOR_URL_BASE + idNumber));
        checkPageScroll(anchorElementLocation.getX(), anchorElementLocation.getY());
    }

    private Long getBrowserHistoryLength() {
        return (Long) executeScript("return window.history.length");
    }
}
