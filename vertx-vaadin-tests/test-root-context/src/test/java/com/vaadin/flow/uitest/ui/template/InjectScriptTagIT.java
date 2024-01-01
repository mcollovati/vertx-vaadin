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
package com.vaadin.flow.uitest.ui.template;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InjectScriptTagIT extends ChromeBrowserTest {

    @Test
    public void openPage_scriptIsEscaped() {
        open();

        TestBenchElement parent = $("inject-script-tag-template").first();

        TestBenchElement div = parent.$(TestBenchElement.class).id("value-div");
        Assert.assertEquals("<!-- <script>", div.getText());

        WebElement slot = findElement(By.id("slot-1"));
        Assert.assertEquals("<!-- <script> --><!-- <script></script>", slot.getText());

        TestBenchElement button = parent.$(TestBenchElement.class).id("change-value");
        button.click();

        Assert.assertEquals("<!-- <SCRIPT>", div.getText());
        slot = findElement(By.id("slot-2"));
        Assert.assertEquals("<!-- <SCRIPT> --><!-- <SCRIPT></SCRIPT>", slot.getText());
    }
}
