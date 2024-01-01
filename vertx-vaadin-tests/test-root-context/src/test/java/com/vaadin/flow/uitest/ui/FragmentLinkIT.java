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
package com.vaadin.flow.uitest.ui;

import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FragmentLinkIT extends ChromeBrowserTest {

    @Test
    public void testInsidePageNavigation_noRouterLinkHandling() {
        open();

        clickScrollerLink2();

        verifyInsideServletLocation("com.vaadin.flow.uitest.ui.FragmentLinkView#Scroll_Target2");
        verifyHashChangeEvents(1);
        verifyScrollTarget2Visible();

        clickScrollerLink();

        verifyInsideServletLocation("com.vaadin.flow.uitest.ui.FragmentLinkView#Scroll_Target");
        verifyHashChangeEvents(2);
        verifyScrollTargetVisible();
    }

    @Test
    @Ignore("Ignored because of fusion issue : https://github.com/vaadin/flow/issues/7575")
    public void testViewChangeWithFragment_scrollToPageAndHashChangeEventWorks() {
        open();

        clickAnotherViewLink();

        verifyInsideServletLocation("com.vaadin.flow.uitest.ui.FragmentLinkView2#Scroll_Target");
        verifyHashChangeEvents(1);
        verifyScrollTargetVisible();
        verifyView2Open();
    }

    @Test
    @Ignore("Ignored because of fusion issue : https://github.com/vaadin/flow/issues/7575")
    public void testViewChangeWithFragment_serverOverridesLocation_noScrollOrHashChange() {
        open();

        clickOverriddenLink();

        verifyInsideServletLocation("overridden#Scroll_Target2");
        // history.replaceState won't fire fragment change
        verifyHashChangeEvents(0);
        verifyTopOfThePage();
    }

    private void clickScrollerLink() {
        clickLink("Scroller link");
    }

    private void clickScrollerLink2() {
        clickLink("Scroller link 2");
    }

    private void clickAnotherViewLink() {
        clickLink("Scroller link with different view");
    }

    private void clickOverriddenLink() {
        clickLink("Link that server overrides");
    }

    private void clickLink(String linkText) {
        WebElement link = findElement(By.linkText(linkText));
        scrollIntoViewAndClick(link);
    }

    private void verifyScrollTargetVisible() {
        int scrollPos = findElement(By.id("Scroll_Target")).getLocation().getY();
        int expected = getScrollLocatorPosition();
        assertScrollPosition(expected, scrollPos);
    }

    private void verifyScrollTarget2Visible() {
        int scrollPos = findElement(By.id("Scroll_Target2")).getLocation().getY();
        int expected = getScrollLocatorPosition();
        assertScrollPosition(expected, scrollPos);
    }

    private int getScrollLocatorPosition() {
        return findElement(By.id("scrollLocator")).getLocation().getY();
    }

    private void verifyTopOfThePage() {
        assertScrollPosition(0, getScrollLocatorPosition());
    }

    private void verifyView2Open() {
        Assert.assertNotNull("FragmentView2 not opened", findElement(By.id("view2")));
    }

    private void assertScrollPosition(int expected, int actual) {
        int lowerBound = expected - 2 > 0 ? expected - 2 : 0;
        int higherBound = expected + 2;
        Assert.assertTrue(
                "Invalid scroll position, expected " + expected + " +-2px. actual " + actual,
                lowerBound <= expected && expected <= higherBound);
    }

    private void verifyHashChangeEvents(int numberOfEvents) {
        List<WebElement> spans = findElement(By.id("placeholder")).findElements(By.tagName("span"));
        Assert.assertEquals("Invalid amount of hash change events", numberOfEvents, spans.size());
    }

    private void verifyInsideServletLocation(String pathAfterServletMapping) {
        Assert.assertEquals(
                "Invalid URL",
                getRootURL() + "/view/" + pathAfterServletMapping,
                getDriver().getCurrentUrl());
    }
}
