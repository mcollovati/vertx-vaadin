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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class FileAccessIT extends ViteDevModeIT {

    @Test
    public void expectedFoldersAccessible() throws Exception {
        /*
         * This just tests a few sample folders to see that there is not a
         * fundamental problem
         */
        assertAllowed("frontend/jsonloader.js");
    }

    private void assertAllowed(String fileInProject) throws IOException {
        String result = IOUtils.toString(getFsUrl(fileInProject), StandardCharsets.UTF_8);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void mostFoldersNotAccessible() throws Exception {
        /*
         * This just tests a few sample folders to see that there is not a
         * fundamental problem
         */
        assertDenied("target/vaadin-dev-server-settings.json");
        assertDenied("pom.xml");
        assertDenied("../pom.xml");
    }

    private void assertDenied(String fileInProject) {
        try {
            URL url = getFsUrl(fileInProject);
            IOUtils.toString(url, StandardCharsets.UTF_8);
            Assert.fail("Request for " + url + " should not succeed");
        } catch (IOException e) {
            Assert.assertTrue(
                    "Request for " + fileInProject + " should have failed",
                    e.getMessage().contains("Server returned HTTP response code: 403"));
        }
    }

    private URL getFsUrl(String fileInProject) throws IOException {
        // For Windows, the URLs should be like
        // http://localhost:8888/VAADIN/@fs/C:/Code/flow/flow-tests/test-frontend/vite-basics/target/vaadin-dev-server-settings.json

        String currentPath = new java.io.File(".").getCanonicalPath().replace("\\", "/");
        if (!currentPath.startsWith("/")) {
            currentPath = "/" + currentPath;
        }
        return new URL("http://localhost:8888/VAADIN/@fs" + currentPath + "/" + fileInProject);
    }
}
