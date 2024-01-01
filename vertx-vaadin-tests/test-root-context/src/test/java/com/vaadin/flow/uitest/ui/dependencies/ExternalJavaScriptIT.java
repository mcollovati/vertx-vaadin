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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ExternalJavaScriptIT extends ChromeBrowserTest {
    // prefix with "http:" since Selenium drives seem to expand url fragments
    // to full length:
    // https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/1824
    private static final String EXPECTED_SRC_FOR_NO_PROTOCOL =
            "http:" + ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL_WITHOUT_PROTOCOL;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("div"));
    }

    @Test
    public void javaScriptAnnotation_externalJs_shouldBeAddedToPage() {
        List<WebElement> scriptTags = findElements(By.tagName("script"));
        Assert.assertTrue(
                "External JS annotated with @JavaScript annotation should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream()
                        .anyMatch(scriptTag -> ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL.equals(
                                        scriptTag.getAttribute("src"))
                                && "text/javascript".equals(scriptTag.getAttribute("type"))));
        Assert.assertTrue(
                "External JS without protocol annotated with @JavaScript annotation should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream()
                        .anyMatch(scriptTag -> EXPECTED_SRC_FOR_NO_PROTOCOL.equals(scriptTag.getAttribute("src"))
                                && "text/javascript".equals(scriptTag.getAttribute("type"))));
    }

    @Test
    public void javaScriptAnnotation_externalJsInAComponentBeingAdded_shouldBeAddedToPage() {
        findElement(By.id("addComponentButton")).click();
        waitForElementPresent(By.id("componentWithExternalJavaScript"));

        List<WebElement> scriptTags = findElements(By.tagName("script"));
        Assert.assertTrue(
                "When a component is added to the page, external JS annotated with @JavaScript annotation in the component should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream()
                        .anyMatch(scriptTag -> ComponentWithExternalJavaScript.SOME_RANDOM_EXTERNAL_JS_URL.equals(
                                        scriptTag.getAttribute("src"))
                                && "text/javascript".equals(scriptTag.getAttribute("type"))));
        Assert.assertTrue(
                "When a component is added to the page, external JS without protocol annotated with @JavaScript annotation in the component should be added as a script tag with text/javascript type to the page!",
                scriptTags.stream()
                        .anyMatch(scriptTag -> EXPECTED_SRC_FOR_NO_PROTOCOL.equals(scriptTag.getAttribute("src"))
                                && "text/javascript".equals(scriptTag.getAttribute("type"))));
    }
}
