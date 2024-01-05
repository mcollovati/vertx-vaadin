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

import com.vaadin.flow.component.html.testbench.ParagraphElement;
import com.vaadin.flow.testutil.DevToolsElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.viteapp.views.empty.MainView;
import org.junit.Assert;
import org.junit.Test;

public class BasicsIT extends ViteDevModeIT {

    @Test
    public void applicationStarts() {
        TestBenchElement header = $("h2").first();
        Assert.assertEquals("This place intentionally left empty", header.getText());
    }

    @Test
    public void noTypescriptErrors() throws Exception {
        // Ensure the file was loaded
        Assert.assertEquals("good", executeScript("return window.bad()"));
        Thread.sleep(2000); // Checking is async so it sometimes needs some time
        Assert.assertFalse(
                "There should be no error overlay",
                $("vite-plugin-checker-error-overlay").exists());
    }

    @Test
    public void imageFromThemeShown() {
        TestBenchElement img = $("img").id(MainView.PLANT);
        waitUntil(driver -> {
            String heightString =
                    (String) executeScript("return getComputedStyle(arguments[0]).height.replace('px','')", img);
            float height = Float.parseFloat(heightString);
            return (height > 150);
        });
    }

    @Test
    public void debugWindowShown() {
        DevToolsElement devTools = $(DevToolsElement.class).waitForFirst();
        devTools.expand();
        Assert.assertNotNull(devTools.$("div")
                .attributeContains("class", "window")
                .attributeContains("class", "visible")
                .waitForFirst());
    }

    @Test
    public void canImportJson() {
        $("button").id(MainView.LOAD_AND_SHOW_JSON).click();
        waitUntil(driver -> $("*").id(MainView.JSON_CONTAINER).getText() != null);
        Assert.assertEquals(
                "{\"hello\":\"World\"}", $("*").id(MainView.JSON_CONTAINER).getText());
    }

    @Test
    public void componentCssDoesNotLeakToDocument() {
        String bodyColor = $("body").first().getCssValue("backgroundColor");
        Assert.assertTrue(
                "Body should be grey, not red as specified for the component", bodyColor.contains("211, 211, 211"));
    }

    @Test
    public void importFromDirectoryWorks() {
        String importResult = $("div").id("directoryImportResult").getText();
        Assert.assertEquals("Directory import ok", importResult);
    }
}
