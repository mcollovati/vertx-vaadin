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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.brotli.dec.BrotliInputStream;
import org.junit.Assert;
import org.junit.Test;

public class CompressionIT {

    private String getRootURL() {
        return "http://localhost:8888";
    }

    @Test
    public void resourcesAvailableAsUncompressed() throws Exception {
        String bundleName = getJsBundleName();

        String file = IOUtils.toString(new URL(getRootURL() + bundleName), StandardCharsets.UTF_8);
        Assert.assertTrue(file.contains("generated-flow-imports"));
    }

    @Test
    public void resourcesAvailableAsBrotli() throws Exception {
        String bundleName = getJsBundleName();

        URL compressedUrl = new URL(getRootURL() + bundleName + ".br");
        BrotliInputStream stream = new BrotliInputStream(compressedUrl.openStream());

        String file = IOUtils.toString(stream, StandardCharsets.UTF_8);
        Assert.assertTrue(file.contains("generated-flow-imports"));
    }

    private String getJsBundleName() throws Exception {
        String indexHtml = IOUtils.toString(new URL(getRootURL() + "/index.html"), StandardCharsets.UTF_8);
        Pattern p = Pattern.compile(".* src=\"./VAADIN/build/([^\"]*).*", Pattern.DOTALL);

        Matcher m = p.matcher(indexHtml);
        if (!m.matches()) {
            throw new IllegalStateException("No script found");
        }
        return "/VAADIN/build/" + m.group(1);
    }
}
