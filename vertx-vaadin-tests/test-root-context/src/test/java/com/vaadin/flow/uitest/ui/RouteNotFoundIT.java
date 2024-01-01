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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;

public abstract class RouteNotFoundIT extends ChromeBrowserTest {
    /*
     * Original script: <img src=x
     * onerror=(function(){d=document.createElement("DIV");document.body.
     * appendChild(d);d.id="injected";})()>
     */
    protected static final String INJECT_ATTACK = "%3Cimg%20src%3Dx%20onerror"
            + "%3D%28function%28%29%7Bd%3Ddocument.createElement%28%22DIV%22%"
            + "29%3Bdocument.body.appendChild%28d%29%3Bd.id%3D%22injected%22%"
            + "3B%7D%29%28%29%3E";

    protected void assertPageHasRoutes(boolean contains) {
        String pageSource = getDriver().getPageSource();
        Assert.assertEquals(contains, pageSource.contains("Available routes"));
        Assert.assertEquals(contains, pageSource.contains("noParent"));
        Assert.assertEquals(contains, pageSource.contains("foo/bar"));
        // check that <img src=x onerror=...> did not inject div via script
        Assert.assertFalse(pageSource.contains("<div id=\"injected\"></div>"));
    }
}
