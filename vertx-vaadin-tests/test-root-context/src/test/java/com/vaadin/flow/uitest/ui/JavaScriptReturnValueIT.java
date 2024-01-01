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

import java.util.Arrays;
import java.util.function.Function;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class JavaScriptReturnValueIT extends ChromeBrowserTest {
    @Test
    public void testAllCombinations() {
        open();

        /*
         * There are 3 * 4 * 2 * 3 = 72 different combinations in the UI, let's
         * test all of them just because we can
         */
        for (String method : Arrays.asList("execPage", "execElement", "callElement")) {
            for (String value : Arrays.asList("string", "number", "null", "error")) {
                for (String outcome : Arrays.asList("success", "failure")) {
                    for (String type : Arrays.asList("synchronous", "resolvedpromise", "timeout")) {
                        testCombination(method, value, outcome, type);
                    }
                }
            }
        }
    }

    private void testCombination(String method, String value, String outcome, String type) {
        String combinationId = String.join(", ", method, value, outcome, type);
        String expectedStatus = getExpectedStatus(value, outcome);

        Function<String, String> inputSelectors = id -> "input#" + id;
        for (String target : Arrays.asList(
                "button#clear",
                inputSelectors.apply(method),
                inputSelectors.apply(value),
                inputSelectors.apply(outcome),
                inputSelectors.apply(type),
                "button#run")) {
            findElement(By.cssSelector(target)).click();
        }

        if ("timeout".equals(type)) {
            try {
                Assert.assertEquals(
                        "Result should not be there immediately for " + combinationId,
                        "Running...",
                        findElement(By.cssSelector("div#status")).getText());

                waitUntil(ExpectedConditions.textToBe(By.cssSelector("div#status"), expectedStatus), 2);
            } catch (TimeoutException e) {
                Assert.fail("Didn't reach expected result for " + combinationId
                        + ". Expected " + expectedStatus + " but got "
                        + findElement(By.cssSelector("div#status")).getText());
                e.printStackTrace();
            }
        } else {
            Assert.assertEquals(
                    "Unexpected result for " + combinationId,
                    expectedStatus,
                    findElement(By.cssSelector("div#status")).getText());
        }
    }

    private String getExpectedStatus(String value, String outcome) {
        String prefix = "";
        if ("failure".equals(outcome)) {
            prefix = "Error: ";

            if ("null".equals(value)) {
                // Special case since the null is handled differently for errors
                // and for results
                return prefix + "null";
            } else if ("error".equals(value)) {
                // Message from inside the Error object should be included
                return prefix + "Error: message";
            }
        }

        switch (value) {
            case "string":
                return prefix + "foo";
            case "number":
                return prefix + "42";
            case "null":
                return prefix;
            case "error":
                // JreJsonObject.asString()
                return prefix + "[object Object]";
            default:
                throw new IllegalArgumentException("Unsupported value type: " + value);
        }
    }
}
