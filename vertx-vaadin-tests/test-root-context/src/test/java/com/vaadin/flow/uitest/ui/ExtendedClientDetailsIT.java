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
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

public class ExtendedClientDetailsIT extends ChromeBrowserTest {

    @Test
    public void testExtendedClientDetails_reportsSomething() {
        open();

        $(TestBenchElement.class).id("fetch-values").click();

        verifyTextMatchesJSExecution("sh", "window.screen.height");
        verifyTextMatchesJSExecution("sw", "window.screen.width");
        verifyTextMatchesJSExecution("wh", "window.innerHeight");
        verifyTextMatchesJSExecution("ww", "window.innerWidth");
        verifyTextMatchesJSExecution("bh", "document.body.clientHeight");
        verifyTextMatchesJSExecution("bw", "document.body.clientWidth");
        try {
            Double.parseDouble($(TestBenchElement.class).id("pr").getText());
        } catch (NumberFormatException nfe) {
            Assert.fail("Could not parse reported device pixel ratio");
        }
        Assert.assertTrue(
                "false".equalsIgnoreCase($(TestBenchElement.class).id("td").getText()));
    }

    @Test
    public void testExtendedClientDetails_predefinedDevicePixelRatioTouchSupport_reportedCorrectly() {
        open();

        $(TestBenchElement.class).id("set-values").click();
        $(TestBenchElement.class).id("fetch-values").click();

        try {
            double pixelRatio =
                    Double.parseDouble($(TestBenchElement.class).id("pr").getText());
            Assert.assertEquals("Invalid Pixel ratio reported", 2.0D, pixelRatio, 0.1D);
        } catch (NumberFormatException nfe) {
            Assert.fail("Could not parse reported device pixel ratio");
        }
        Assert.assertTrue(
                "true".equalsIgnoreCase($(TestBenchElement.class).id("td").getText()));
    }

    private void verifyTextMatchesJSExecution(String elementId, String jsExecution) {
        String elementText = $(TestBenchElement.class).id(elementId).getText();
        Object executionResult = getCommandExecutor().executeScript(("return " + jsExecution + ";"));
        Assert.assertEquals(
                "reported value did not match js execution for " + elementId, executionResult.toString(), elementText);
    }
}
