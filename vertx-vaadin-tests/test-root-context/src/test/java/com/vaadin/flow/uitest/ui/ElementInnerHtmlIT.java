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

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;

public class ElementInnerHtmlIT extends ChromeBrowserTest {

    @Test
    public void elementInitOrder() {
        open();
        DivElement innerHtml = $(DivElement.class).id("inner-html-field");

        Assert.assertEquals("", innerHtml.getPropertyString("innerHTML"));

        $(NativeButtonElement.class).id("set-foo").click();
        Assert.assertEquals("<p>Foo</p>", innerHtml.getPropertyString("innerHTML"));

        $(NativeButtonElement.class).id("set-foo").click();
        Assert.assertEquals("<p>Foo</p>", innerHtml.getPropertyString("innerHTML"));

        $(NativeButtonElement.class).id("set-boo").click();
        Assert.assertEquals("<p>Boo</p>", innerHtml.getPropertyString("innerHTML"));

        $(NativeButtonElement.class).id("set-boo").click();
        Assert.assertEquals("<p>Boo</p>", innerHtml.getPropertyString("innerHTML"));

        $(NativeButtonElement.class).id("set-null").click();
        Assert.assertEquals("", innerHtml.getPropertyString("innerHTML"));
    }
}
