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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class StreamResourceIT extends ChromeBrowserTest {

    @Test
    public void getDynamicVaadinResource() throws IOException {
        open();

        WebElement link = findElement(By.cssSelector("a#link"));
        String url = link.getAttribute("href");

        getDriver().manage().timeouts().setScriptTimeout(15, TimeUnit.SECONDS);

        try (InputStream stream = download(url)) {
            List<String> lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
            String text = lines.stream().collect(Collectors.joining());
            Assert.assertEquals("foo", text);
        }
    }

    /*
     * Stolen from stackexchange.
     *
     * It's not possible to use a straight way to download the link externally
     * since it will use another session and the link will be invalid in this
     * session. So either this pure client side way or external download with
     * cookies copy (which allows preserve the session) needs to be used.
     */
    public InputStream download(String url) throws IOException {
        String script = "var url = arguments[0];"
                + "var callback = arguments[arguments.length - 1];"
                + "var xhr = new XMLHttpRequest();"
                + "xhr.open('GET', url, true);"
                + "xhr.responseType = \"arraybuffer\";" +
                // force the HTTP response, response-type header to be array
                // buffer
                "xhr.onload = function() {"
                + "  var arrayBuffer = xhr.response;"
                + "  var byteArray = new Uint8Array(arrayBuffer);"
                + "  callback(byteArray);" + "};" + "xhr.send();";
        Object response = ((JavascriptExecutor) getDriver()).executeAsyncScript(script, url);
        // Selenium returns an Array of Long, we need byte[]
        ArrayList<?> byteList = (ArrayList<?>) response;
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            Long byt = (Long) byteList.get(i);
            bytes[i] = byt.byteValue();
        }
        return new ByteArrayInputStream(bytes);
    }
}
