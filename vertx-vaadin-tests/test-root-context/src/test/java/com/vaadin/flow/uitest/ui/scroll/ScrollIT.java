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

import java.util.Objects;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScrollIT extends AbstractScrollIT {

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7584")
    public void scrollPositionIsRestoredAfterNavigatingToNewPageAndBack() {
        open();

        final String initialPageUrl = driver.getCurrentUrl();
        final int xScrollAmount = 0;
        final int yScrollAmount = 400;

        scrollBy(xScrollAmount, yScrollAmount);
        checkPageScroll(xScrollAmount, yScrollAmount);

        clickElementWithJs(ScrollView.TRANSITION_URL_ID);

        while (true) {
            if (Objects.equals(initialPageUrl, driver.getCurrentUrl())) {
                checkPageScroll(xScrollAmount, yScrollAmount);
            } else {
                ensureThatNewPageIsNotScrolled();
                break;
            }
        }

        findElement(By.id(LongToOpenView.BACK_BUTTON_ID)).click();

        assertThat("Did not return back on initial page", driver.getCurrentUrl(), is(initialPageUrl));
        checkPageScroll(xScrollAmount, yScrollAmount);
    }

    @Test
    public void anchorUrlsWorkProperly() {
        open();

        final int xScrollAmount = 0;
        final int yScrollAmount = 400;

        Point anchorElementLocation =
                findElement(By.id(ScrollView.ANCHOR_DIV_ID)).getLocation();

        scrollBy(xScrollAmount, yScrollAmount);
        checkPageScroll(xScrollAmount, yScrollAmount);

        clickElementWithJs(ScrollView.SIMPLE_ANCHOR_URL_ID);
        checkPageScroll(anchorElementLocation.getX(), anchorElementLocation.getY());
        assertThat("Expected url to change to anchor one", driver.getCurrentUrl(), endsWith(ScrollView.ANCHOR_URL));

        scrollBy(xScrollAmount, yScrollAmount);
        clickElementWithJs(ScrollView.ROUTER_ANCHOR_URL_ID);
        checkPageScroll(anchorElementLocation.getX(), anchorElementLocation.getY());
        assertThat("Expected url to change to anchor one", driver.getCurrentUrl(), endsWith(ScrollView.ANCHOR_URL));
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7584")
    public void scrollPositionIsRestoredWhenNavigatingToHistoryWithAnchorLink() {
        open();

        clickElementWithJs(ScrollView.ROUTER_ANCHOR_URL_ID);
        assertThat("Expected url to change to anchor one", driver.getCurrentUrl(), endsWith(ScrollView.ANCHOR_URL));

        scrollBy(0, 400);
        final int originalScrollX = getScrollX();
        final int originalScrollY = getScrollY();

        clickElementWithJs(ScrollView.TRANSITION_URL_ID);
        findElement(By.id(LongToOpenView.BACK_BUTTON_ID)).click();

        assertThat("Expected url to change to anchor one", driver.getCurrentUrl(), endsWith(ScrollView.ANCHOR_URL));
        checkPageScroll(originalScrollX, originalScrollY);
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/7584")
    public void scrollPositionShouldBeAtAnchorWhenNavigatingFromOtherPage() {
        open();

        Point anchorElementLocation =
                findElement(By.id(ScrollView.ANCHOR_DIV_ID)).getLocation();
        scrollBy(0, 400);

        clickElementWithJs(ScrollView.TRANSITION_URL_ID);
        clickElementWithJs(LongToOpenView.ANCHOR_LINK_ID);
        assertThat("Expected url to change to anchor one", driver.getCurrentUrl(), endsWith(ScrollView.ANCHOR_URL));
        checkPageScroll(anchorElementLocation.getX(), anchorElementLocation.getY());
    }
}
