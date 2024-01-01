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
package com.vaadin.flow.uitest.ui.dependencies;

import java.util.List;
import java.util.logging.Level;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntry;

public class DynamicDependencyIT extends ChromeBrowserTest {

    @Test
    public void dynamicDependencyIsExecutedBeforeOtherMessageProcessing() {
        open();

        WebElement depElement = findElement(By.id("dep"));
        // true means that the added component (a new one) is not yet in the DOM
        Assert.assertEquals(Boolean.TRUE.toString(), depElement.getText());
    }

    @Test
    public void dependecyIsNoPromise_errorLogged() {
        testErrorCase("nopromise", "result is not a Promise");
    }

    @Test
    public void dependecyLoaderThrows_errorLogged() throws InterruptedException {
        testErrorCase("throw", "Throw on purpose");
    }

    @Test
    public void dependecyLoaderRejects_errorLogged() throws InterruptedException {
        testErrorCase("reject", "Reject on purpose");
    }

    private void testErrorCase(String caseName, String errorMessageSnippet) {
        open();

        findElement(By.id(caseName)).click();

        String statusText = findElement(By.id("new-component")).getText();
        Assert.assertEquals("Div updated for " + caseName, statusText);

        List<LogEntry> entries = getLogEntries(Level.SEVERE);
        Assert.assertEquals(2, entries.size());

        Assert.assertThat(entries.get(0).getMessage(), Matchers.containsString(errorMessageSnippet));
        Assert.assertThat(entries.get(1).getMessage(), Matchers.containsString("could not be loaded"));
    }
}
