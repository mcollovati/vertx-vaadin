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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class SynchronizedPropertyIT extends ChromeBrowserTest {

    @Test
    public void synchronizeOnChange() {
        open();
        WebElement syncOnChange = findElement(By.id("syncOnChange"));
        WebElement labelSyncOnChange = findElement(By.id("syncOnChangeLabel"));
        syncOnChange.sendKeys("123");
        blur();
        Assert.assertEquals("Server value: 123", labelSyncOnChange.getText());
        syncOnChange.sendKeys("456");
        blur();
        Assert.assertEquals("Server value: 123456", labelSyncOnChange.getText());
    }

    @Test
    public void synchronizeOnKeyUp() {
        open();
        WebElement syncOnKeyUp = findElement(By.id("syncOnKeyUp"));
        WebElement labelSyncOnKeyUp = findElement(By.id("syncOnKeyUpLabel"));
        syncOnKeyUp.sendKeys("1");
        syncOnKeyUp.sendKeys("2");
        syncOnKeyUp.sendKeys("3");
        Assert.assertEquals("Server value: 123", labelSyncOnKeyUp.getText());
        syncOnKeyUp.sendKeys("4");
        syncOnKeyUp.sendKeys("5");
        syncOnKeyUp.sendKeys("6");
        Assert.assertEquals("Server value: 123456", labelSyncOnKeyUp.getText());
    }

    @Test
    public void synchronizeInitialValueNotSentToServer() {
        open();
        WebElement syncOnChangeInitialValue = findElement(By.id("syncOnChangeInitialValue"));
        WebElement labelSyncOnChange = findElement(By.id("syncOnChangeInitialValueLabel"));

        // Property was set after label was created and sync set up
        // It is intentionally in the "wrong" state until there is a sync
        // message from the client
        Assert.assertEquals("Server value on create: null", labelSyncOnChange.getText());
        syncOnChangeInitialValue.sendKeys(Keys.END);
        syncOnChangeInitialValue.sendKeys("123");
        blur();
        Assert.assertEquals("Server value in change listener: initial123", labelSyncOnChange.getText());
    }

    @Test
    public void synchronizeMultipleProperties() {
        open();
        WebElement multiSyncValue = findElement(By.id("multiSyncValue"));
        WebElement multiSyncValueLabel = findElement(By.id("multiSyncValueLabel"));
        WebElement multiSyncValueAsNumberLabel = findElement(By.id("multiSyncValueAsNumberLabel"));
        multiSyncValue.sendKeys("123");
        waitUntil(driver -> "Server value: 123".equals(multiSyncValueLabel.getText()), 2);
        Assert.assertEquals("", multiSyncValueAsNumberLabel.getText());
        blur();
        waitUntil(driver -> "Server value: 123".equals(multiSyncValueLabel.getText()), 2);
        Assert.assertEquals("Server valueAsNumber: 123", multiSyncValueAsNumberLabel.getText());

        multiSyncValue.sendKeys("456");
        waitUntil(driver -> "Server value: 123456".equals(multiSyncValueLabel.getText()), 2);
        Assert.assertEquals("Server valueAsNumber: 123", multiSyncValueAsNumberLabel.getText());
        blur();
        waitUntil(driver -> "Server value: 123456".equals(multiSyncValueLabel.getText()), 2);
        Assert.assertEquals("Server valueAsNumber: 123456", multiSyncValueAsNumberLabel.getText());
    }
}
