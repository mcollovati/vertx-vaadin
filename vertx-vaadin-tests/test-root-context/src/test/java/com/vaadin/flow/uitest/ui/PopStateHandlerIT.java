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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class PopStateHandlerIT extends ChromeBrowserTest {

    private static final String FORUM = "com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/";
    private static final String FORUM_SUBCATEGORY = "com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#!/category/1";
    private static final String FORUM_SUBCATEGORY2 = "com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#!/category/2";
    private static final String ANOTHER_PATH = "com.vaadin.flow.uitest.ui.PopStateHandlerUI/another/";
    private static final String EMPTY_HASH = "com.vaadin.flow.uitest.ui.PopStateHandlerUI/forum/#";

    @Test
    public void testDifferentPath_ServerSideEvent() {
        open();
        verifyNoServerVisit();
        verifyInsideServletLocation(getViewClass().getName());

        pushState(FORUM);

        verifyInsideServletLocation(FORUM);
        verifyNoServerVisit();

        pushState(ANOTHER_PATH);

        verifyInsideServletLocation(ANOTHER_PATH);
        verifyNoServerVisit();

        goBack();
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/10485")
    public void testDifferentPath_doubleBack_ServerSideEvent() {
        open();

        pushState(FORUM);
        pushState(ANOTHER_PATH);

        goBack();

        verifyPopStateEvent(FORUM);
        verifyInsideServletLocation(FORUM);

        goBack();

        verifyPopStateEvent(getViewClass().getName());
        verifyInsideServletLocation(getViewClass().getName());
    }

    @Test
    public void testSamePathHashChanges_noServerSideEvent() {
        open();
        verifyNoServerVisit();
        verifyInsideServletLocation(getViewClass().getName());

        pushState(FORUM);

        verifyInsideServletLocation(FORUM);
        verifyNoServerVisit();

        pushState(FORUM_SUBCATEGORY);

        verifyInsideServletLocation(FORUM_SUBCATEGORY);
        verifyNoServerVisit();

        pushState(FORUM_SUBCATEGORY2);

        verifyInsideServletLocation(FORUM_SUBCATEGORY2);
        verifyNoServerVisit();

        goBack();
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/10485")
    public void testSamePathHashChanges_tripleeBack_noServerSideEvent() {
        open();

        pushState(FORUM);

        pushState(FORUM_SUBCATEGORY);

        pushState(FORUM_SUBCATEGORY2);

        goBack();

        verifyNoServerVisit();
        verifyInsideServletLocation(FORUM_SUBCATEGORY);

        goBack();

        verifyNoServerVisit();
        verifyInsideServletLocation(FORUM);

        goBack();

        verifyPopStateEvent(getViewClass().getName());
        verifyInsideServletLocation(getViewClass().getName());
    }

    @Test
    public void testEmptyHash_noHashServerToServer() {
        open();
        verifyNoServerVisit();
        verifyInsideServletLocation(getViewClass().getName());

        pushState(EMPTY_HASH);

        verifyInsideServletLocation(EMPTY_HASH);
        verifyNoServerVisit();

        pushState(FORUM);

        verifyInsideServletLocation(FORUM);
        verifyNoServerVisit();

        pushState(EMPTY_HASH);

        verifyInsideServletLocation(EMPTY_HASH);
        verifyNoServerVisit();

        pushState(ANOTHER_PATH);

        verifyInsideServletLocation(ANOTHER_PATH);
        verifyNoServerVisit();

        goBack();
    }

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/10485")
    public void testEmptyHash_quadrupleBack_noHashServerToServer() {
        open();

        pushState(EMPTY_HASH);

        pushState(FORUM);

        pushState(EMPTY_HASH);

        pushState(ANOTHER_PATH);

        goBack();

        verifyPopStateEvent(FORUM);
        verifyInsideServletLocation(EMPTY_HASH);

        goBack();

        verifyPopStateEvent(FORUM);
        verifyInsideServletLocation(FORUM);

        goBack();

        verifyPopStateEvent(FORUM);
        verifyInsideServletLocation(EMPTY_HASH);

        goBack();

        verifyPopStateEvent(getViewClass().getName());
        verifyInsideServletLocation(getViewClass().getName());
    }

    private void goBack() {
        executeScript("window.history.back()");
    }

    private void pushState(String id) {
        findElement(By.id(id)).click();
    }

    private void verifyInsideServletLocation(String pathAfterServletMapping) {
        Assert.assertEquals(
                "Invalid URL",
                getRootURL() + "/view/" + pathAfterServletMapping,
                getDriver().getCurrentUrl());
    }

    private void verifyNoServerVisit() {
        verifyPopStateEvent("no location");
    }

    private void verifyPopStateEvent(String location) {
        Assert.assertEquals(
                "Invalid server side event location",
                location,
                findElement(By.id("location")).getText());
    }
}
