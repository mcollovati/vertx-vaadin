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
package com.vaadin.viteapp;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.viteapp.views.empty.MainView;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Dimension;

public class ProductionBasicsIT extends ChromeBrowserTest {

    @Test
    public void applicationStarts() {
        getDriver().get(getRootURL());
        waitForDevServer();
        TestBenchElement header = $("h2").first();
        Assert.assertEquals("This place intentionally left empty", header.getText());
        Assert.assertFalse((Boolean) getCommandExecutor().executeScript("return Vaadin.developmentMode"));
    }

    @Test
    public void imageFromThemeShown() {
        getDriver().get(getRootURL());
        waitForDevServer();
        TestBenchElement img = $("img").id(MainView.PLANT);
        waitUntil(driver -> {
            String heightString =
                    (String) executeScript("return getComputedStyle(arguments[0]).height.replace('px','')", img);
            float height = Float.parseFloat(heightString);
            return (height > 150);
        });
    }

    @Test
    public void imageCanBeHidden() {
        getDriver().get(getRootURL());
        waitForDevServer();
        TestBenchElement img = $("img").id(MainView.PLANT);
        TestBenchElement button = $("button").id(MainView.HIDEPLANT);
        button.click();
        Assert.assertEquals("none", img.getCssValue("display"));
    }

    @Test
    public void applicationHasThemeAndAssets() {
        getDriver().get(getRootURL());
        waitForDevServer();

        String pColor = $("p").first().getCssValue("color");
        Assert.assertEquals("rgba(0, 100, 0, 1)", pColor);

        Dimension size = $("img").first().getSize();
        Assert.assertEquals(200, size.getWidth());
        Assert.assertEquals(200, size.getHeight());
    }
}
