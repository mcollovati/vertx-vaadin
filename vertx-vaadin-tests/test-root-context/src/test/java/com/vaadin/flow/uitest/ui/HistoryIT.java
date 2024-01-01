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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class HistoryIT extends ChromeBrowserTest {

    @Test
    @Ignore("Ignored because of fusion issue: https://github.com/vaadin/flow/issues/8213")
    public void testHistory() throws URISyntaxException {
        open();

        URI baseUrl = getCurrentUrl();

        InputTextElement stateField = $(InputTextElement.class).id("state");
        InputTextElement locationField = $(InputTextElement.class).id("location");
        WebElement pushButton = findElement(By.cssSelector("button#pushState"));
        WebElement replaceButton = findElement(By.cssSelector("button#replaceState"));
        WebElement backButton = findElement(By.cssSelector("button#back"));
        WebElement forwardButton = findElement(By.cssSelector("button#forward"));
        WebElement clearButton = findElement(By.cssSelector("button#clear"));

        stateField.setValue("{'foo':true}");
        locationField.setValue("asdf");
        pushButton.click();

        Assert.assertEquals(baseUrl.resolve("asdf"), getCurrentUrl());

        // Back to original state
        backButton.click();

        Assert.assertEquals(baseUrl, getCurrentUrl());
        Assert.assertEquals(Arrays.asList("New location: com.vaadin.flow.uitest.ui.HistoryView"), getStatusMessages());
        clearButton.click();

        stateField.clear();
        locationField.clear();
        locationField.setValue("qwerty");
        replaceButton.click();

        Assert.assertEquals(baseUrl.resolve("qwerty"), getCurrentUrl());

        // Forward to originally pushed state
        forwardButton.click();
        Assert.assertEquals(baseUrl.resolve("asdf"), getCurrentUrl());
        Assert.assertEquals(Arrays.asList("New location: asdf", "New state: {\"foo\":true}"), getStatusMessages());
        clearButton.click();

        // Back to the replaced state
        backButton.click();

        Assert.assertEquals(baseUrl.resolve("qwerty"), getCurrentUrl());
        Assert.assertEquals(Arrays.asList("New location: qwerty"), getStatusMessages());

        // Navigate to empty string should go to the context path root
        stateField.clear();
        locationField.clear();
        pushButton.click();

        Assert.assertEquals(baseUrl.resolve("."), getCurrentUrl());
    }

    private URI getCurrentUrl() throws URISyntaxException {
        URI uri = new URI(getDriver().getCurrentUrl());
        return uri;
    }

    private List<String> getStatusMessages() {
        return findElements(By.className("status")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
}
