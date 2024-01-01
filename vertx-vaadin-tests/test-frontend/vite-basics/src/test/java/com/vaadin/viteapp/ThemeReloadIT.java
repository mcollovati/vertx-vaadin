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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vaadin.testbench.TestBenchElement;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class ThemeReloadIT extends ViteDevModeIT {

    @Test
    @Ignore
    public void updateStyle_changeIsReloaded() throws IOException {
        TestBenchElement header = $("h2").first();
        Assert.assertEquals("rgba(0, 0, 255, 1)", header.getCssValue("color"));

        File baseDir = new File(System.getProperty("user.dir", "."));
        File themeFolder = new File(baseDir, "frontend/themes/vite-basics/");
        File stylesCss = new File(themeFolder, "styles.css");
        final String stylesContent = FileUtils.readFileToString(stylesCss, StandardCharsets.UTF_8);
        try {
            FileUtils.write(
                    stylesCss, stylesContent + "\nh2 { color: rgba(255, 0, 0, 1); }", StandardCharsets.UTF_8.name());

            waitUntil(
                    webDriver -> webDriver
                            .findElement(By.tagName("h2"))
                            .getCssValue("color")
                            .equals("rgba(255, 0, 0, 1)"),
                    30);
            header = $("h2").first();
            Assert.assertEquals("rgba(255, 0, 0, 1)", header.getCssValue("color"));
        } finally {
            FileUtils.write(stylesCss, stylesContent, StandardCharsets.UTF_8.name());
        }
    }
}
