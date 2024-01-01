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
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class KeyboardEventIT extends ChromeBrowserTest {
    @Test
    public void verify_that_keys_are_received_correctly() {
        open();

        // make sure both elements are present
        Assert.assertTrue(isElementPresent(By.cssSelector("input#input")));
        Assert.assertTrue(isElementPresent(By.cssSelector("p#paragraph")));

        WebElement input = findElement(By.cssSelector("input#input"));
        WebElement paragraph = findElement(By.cssSelector("p#paragraph"));

        input.sendKeys("q");

        Assert.assertEquals("q:KeyQ", paragraph.getText());

        input.sendKeys("%");

        Assert.assertEquals("%:Digit5", paragraph.getText());
        // next tests rely on
        // https://github.com/SeleniumHQ/selenium/blob/master/javascript/node/selenium-webdriver/lib/input.js#L52

        // arrow right
        input.sendKeys("\uE014");

        Assert.assertEquals("ArrowRight:ArrowRight", paragraph.getText());

        // physical * key
        input.sendKeys("\uE024");

        Assert.assertEquals("*:NumpadMultiply", paragraph.getText());
    }

    @Test // #5989
    public void verify_that_invalid_keyup_event_is_ignored() {
        open();

        WebElement input = findElement(By.cssSelector("input#input"));
        WebElement sendInvalidKeyUp = findElement(By.id("sendInvalidKeyUp"));
        WebElement paragraph = findElement(By.id("keyUpParagraph"));

        input.sendKeys("q");
        Assert.assertEquals("q", paragraph.getText());

        sendInvalidKeyUp.click();

        Assert.assertEquals("q", paragraph.getText());
    }
}
