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

import com.vaadin.flow.data.value.ValueChangeMode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ValueChangeModeIT extends AbstractDebounceSynchronizeIT {

    private WebElement input;

    @Before
    public void setUp() {
        open();
        input = findElement(By.cssSelector("input#input"));
    }

    @Test
    public void eager() {
        toggleMode(ValueChangeMode.EAGER);
        assertEager(input);
    }

    @Test
    public void lazy() throws InterruptedException {
        toggleMode(ValueChangeMode.LAZY);
        assertDebounce(input);
    }

    @Test
    @Ignore
    public void timeout() throws InterruptedException {
        toggleMode(ValueChangeMode.TIMEOUT);
        assertThrottle(input);
    }

    @Test
    public void onChange() {
        toggleMode(ValueChangeMode.ON_CHANGE);
        input.sendKeys("a");
        assertMessages();

        input.sendKeys("\n");
        assertMessages("a");
    }

    @Test
    public void onBlur() {
        toggleMode(ValueChangeMode.ON_BLUR);
        input.sendKeys("a");
        assertMessages();

        input.sendKeys("\n");
        assertMessages();

        blur();
        assertMessages("a");
    }

    private void toggleMode(ValueChangeMode mode) {
        WebElement modeButton = findElement(By.cssSelector("button#" + mode.name()));
        modeButton.click();
    }
}
