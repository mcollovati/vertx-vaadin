/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.quarkus.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Assert;
import org.openqa.selenium.By;

public abstract class AbstractCdiIT extends AbstractChromeIT {

    protected void click(String elementId) {
        findElement(By.id(elementId)).click();
    }

    protected void follow(String linkText) {
        findElement(By.linkText(linkText)).click();
    }

    protected String getText(String id) {
        return findElement(By.id(id)).getText();
    }

    protected void assertCountEquals(int expectedCount, String counter) throws IOException {
        Assert.assertEquals(expectedCount, getCount(counter));
    }

    protected void assertTextEquals(String expectedText, String elementId) {
        Assert.assertEquals(expectedText, getText(elementId));
    }

    protected void resetCounts() throws IOException {
        slurp("?resetCounts");
    }

    protected int getCount(String id) throws IOException {
        getCommandExecutor().waitForVaadin();
        String line = slurp("?getCount=" + id);
        return Integer.parseInt(line);
    }

    private String slurp(String uri) throws IOException {
        URL url = new URL(getRootURL() + uri);
        InputStream is = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line = reader.readLine();
        reader.close();
        return line;
    }
}
