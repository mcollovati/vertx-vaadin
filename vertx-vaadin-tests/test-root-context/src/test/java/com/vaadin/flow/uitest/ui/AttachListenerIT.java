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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AttachListenerIT extends ChromeBrowserTest {

    @Before
    public void init() {
        open();
    }

    @Test
    public void firstAddedToMiddleOnFirstAttach() {
        assertCombination("middleAsHost", "firstAsChild", "attachListenerToFirst", "MiddleFirstLast");
    }

    @Test
    public void firstAddedToMiddleOnMiddleAttach() {
        assertCombination("middleAsHost", "firstAsChild", "attachListenerToMiddle", "MiddleFirstLast");
    }

    @Test
    public void firstAddedToMiddleOnLastAttach() {
        assertCombination("middleAsHost", "firstAsChild", "attachListenerToLast", "MiddleFirstLast");
    }

    @Test
    public void firstAddedToLastOnFirstAttach() {
        assertCombination("lastAsHost", "firstAsChild", "attachListenerToFirst", "MiddleLastFirst");
    }

    @Test
    public void firstAddedToLastOnMiddleAttach() {
        assertCombination("lastAsHost", "firstAsChild", "attachListenerToMiddle", "MiddleLastFirst");
    }

    @Test
    public void firstAddedToLastOnLastAttach() {
        assertCombination("lastAsHost", "firstAsChild", "attachListenerToLast", "MiddleLastFirst");
    }

    @Test
    public void lastAddedToMiddleOnFirstAttach() {
        assertCombination("middleAsHost", "lastAsChild", "attachListenerToFirst", "FirstMiddleLast");
    }

    @Test
    public void lastAddedToMiddleOnMiddleAttach() {
        assertCombination("middleAsHost", "lastAsChild", "attachListenerToMiddle", "FirstMiddleLast");
    }

    @Test
    public void lastAddedToMiddleOnLastAttach() {
        assertCombination("middleAsHost", "lastAsChild", "attachListenerToLast", "FirstMiddleLast");
    }

    @Test
    public void middleAddedToLastOnFirstAttach() {
        assertCombination("lastAsHost", "middleAsChild", "attachListenerToFirst", "FirstLastMiddle");
    }

    @Test
    public void middleAddedToLastOnMiddleAttach() {
        assertCombination("lastAsHost", "middleAsChild", "attachListenerToMiddle", "FirstLastMiddle");
    }

    @Test
    public void middleAddedToLastOnLastAttach() {
        assertCombination("lastAsHost", "middleAsChild", "attachListenerToLast", "FirstLastMiddle");
    }

    @Test
    public void middleAddedToFirstOnFirstAttach() {
        assertCombination("firstAsHost", "middleAsChild", "attachListenerToFirst", "FirstMiddleLast");
    }

    @Test
    public void middleAddedToFirstOnMiddleAttach() {
        assertCombination("firstAsHost", "middleAsChild", "attachListenerToMiddle", "FirstMiddleLast");
    }

    @Test
    public void middleAddedToFirstOnLastAttach() {
        assertCombination("firstAsHost", "middleAsChild", "attachListenerToLast", "FirstMiddleLast");
    }

    @Test
    public void lastAddedToFirstOnFirstAttach() {
        assertCombination("firstAsHost", "lastAsChild", "attachListenerToFirst", "FirstLastMiddle");
    }

    @Test
    public void lastAddedToFirstOnMiddleAttach() {
        assertCombination("firstAsHost", "lastAsChild", "attachListenerToMiddle", "FirstLastMiddle");
    }

    @Test
    public void lastAddedToFirstOnLastAttach() {
        assertCombination("firstAsHost", "lastAsChild", "attachListenerToLast", "FirstLastMiddle");
    }

    private void assertCombination(String host, String child, String listener, String expectedResult) {
        WebElement hostRadio = findElement(By.id(host));
        WebElement childRadio = findElement(By.id(child));
        WebElement listenerRadio = findElement(By.id(listener));

        hostRadio.click();
        childRadio.click();
        listenerRadio.click();

        findElement(By.id("submit")).click();

        waitForElementPresent(By.id("result"));

        WebElement result = findElement(By.id("result"));
        Assert.assertEquals(expectedResult, result.getText());
    }
}
