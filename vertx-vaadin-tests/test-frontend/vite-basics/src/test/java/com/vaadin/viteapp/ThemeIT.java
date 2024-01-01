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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ThemeIT extends ViteDevModeIT {

    @Test
    public void themeStylesShouldNotBeAddedToHead() {
        String styleFromTheme = "color: darkgreen";

        List<String> adoptedStyleSheetsWithString = (List<String>) executeScript(
                "return document.adoptedStyleSheets.map(sheet => sheet.cssRules).flatMap(rules => Array.from(rules).map(rule => rule.cssText)).filter(rule => rule.includes(arguments[0]))",
                styleFromTheme);
        List<String> styleTagsWithString = (List<String>) executeScript(
                "return Array.from(document.querySelectorAll('style')).map(style => style.textContent).filter(text => text.includes(arguments[0]))",
                styleFromTheme);

        Assert.assertEquals(
                "Theme rule should have been added once using adoptedStyleSheets",
                1,
                adoptedStyleSheetsWithString.size());
        Assert.assertEquals("Theme rule should not have been added to <head>", 0, styleTagsWithString.size());
    }

    @Test
    public void cssImportAnnotation() {
        String bodyBackground = (String) executeScript("return getComputedStyle(document.body).backgroundColor");
        Assert.assertEquals("rgb(211, 211, 211)", bodyBackground);
    }

    @Test
    public void cssImportAnnotationForComponent() {
        String fieldBackground = (String)
                executeScript("return getComputedStyle(document.querySelector('#themedfield')).backgroundColor");
        Assert.assertEquals("rgb(173, 216, 230)", fieldBackground);
    }

    @Test
    public void documentCssImport_externalAddedToHeadAsLink() {
        checkLogsForErrors();

        final WebElement documentHead = getDriver().findElement(By.tagName("head"));
        final List<WebElement> links = documentHead.findElements(By.tagName("link"));

        List<String> linkUrls =
                links.stream().map(link -> link.getAttribute("href")).collect(Collectors.toList());

        Assert.assertTrue(
                "Missing link for external url", linkUrls.contains("https://fonts.googleapis.com/css?family=Itim"));
    }
}
