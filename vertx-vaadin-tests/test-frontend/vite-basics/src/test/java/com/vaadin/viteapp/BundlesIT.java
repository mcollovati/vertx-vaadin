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

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

public class BundlesIT extends ViteDevModeIT {

    @Test
    public void bundlesIsUsed() {
        Assert.assertTrue((Boolean) $("testscope-button").first().getProperty("isFromBundle"));
    }

    @Test
    public void bundleExportWorks() {
        Assert.assertTrue((Boolean) executeScript("return !!window.BundleButtonClass"));
    }

    @Test // for https://github.com/vaadin/flow/issues/14355
    public void bundleDefaultExportWorks() {
        waitUntilNot(driver ->
                driver.findElement(By.tagName("testscope-map")).getText().isEmpty());
        Assert.assertTrue((Boolean) executeScript("return !!window.BundleMapClass"));
        Assert.assertTrue((Boolean) $("testscope-map").first().getProperty("isFromBundle"));
    }

    @Test
    public void optimizeDepsConfigHasEntrypoint() {
        Assert.assertTrue((Boolean)
                executeScript("return window.ViteConfigOptimizeDeps.entries.includes('generated/vaadin.ts')"));
    }

    @Test
    public void optimizeDepsExcludesBundles() {
        Assert.assertTrue(isExcluded("@vaadin/bundles"));
    }

    @Test
    public void optimizeDepsExcludeBundleContents() {
        Assert.assertTrue(isExcluded("@testscope/all"));
        Assert.assertTrue(isExcluded("@testscope/button"));
        Assert.assertTrue(isExcluded("@testscope/map"));
    }

    private boolean isExcluded(String dependency) {
        return (Boolean) executeScript(
                "return (window.ViteConfigOptimizeDeps.exclude || []).includes(arguments[0]);", dependency);
    }
}
