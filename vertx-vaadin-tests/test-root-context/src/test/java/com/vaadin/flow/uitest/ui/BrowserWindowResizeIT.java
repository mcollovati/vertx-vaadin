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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public class BrowserWindowResizeIT extends ChromeBrowserTest {

    @Test
    public void listenResizeEvent() throws InterruptedException {
        open();

        Dimension currentSize = getDriver().manage().window().getSize();

        int newWidth = currentSize.getWidth() - 10;
        int newHeight = currentSize.getHeight() - 10;
        getDriver().manage().window()
                .setSize(new Dimension(newWidth, newHeight));

        // debounced by default with 300
        Thread.sleep(500);

        WebElement info = findElement(By.id("size-info"));

        Assert.assertEquals(String.valueOf(newWidth),
                info.getText().split("x")[0]);
        // Selenium sets the window size, the tested API reports viewport
        // size...
        int actualHeight = Integer.parseInt(info.getText().split("x")[1]);

        newWidth -= 30;
        newHeight -= 20;
        actualHeight -= 20;
        getDriver().manage().window()
                .setSize(new Dimension(newWidth, newHeight));

        Thread.sleep(500);

        Assert.assertEquals(String.valueOf(newWidth),
                info.getText().split("x")[0]);
        Assert.assertEquals(String.valueOf(actualHeight),
                info.getText().split("x")[1]);

        // check the same comes in still if modal component set
        findElement(By.id("modal")).click();
        newWidth += 30;
        newHeight += 20;
        actualHeight += 20;
        getDriver().manage().window()
                .setSize(new Dimension(newWidth, newHeight));

        Thread.sleep(500);

        Assert.assertEquals(String.valueOf(newWidth),
                info.getText().split("x")[0]);
        Assert.assertEquals(String.valueOf(actualHeight),
                info.getText().split("x")[1]);
    }
}
