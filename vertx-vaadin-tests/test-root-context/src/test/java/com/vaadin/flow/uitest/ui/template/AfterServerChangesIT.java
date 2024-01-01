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

import java.util.List;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AfterServerChangesIT extends ChromeBrowserTest {

    @Test
    public void notifyServerUpdateOnTheClientSide() {
        open();

        List<TestBenchElement> components = $("after-server-changes").all();
        components.forEach(component -> assertAfterServerUpdate(component, 1));

        WebElement update = findElement(By.id("update"));

        update.click();

        components.forEach(component -> assertAfterServerUpdate(component, 2));

        findElement(By.id("remove")).click();

        update.click();

        // The second components is removed
        // No exceptions , everything is functional
        assertAfterServerUpdate($("after-server-changes").first(), 3);
    }

    private void assertAfterServerUpdate(TestBenchElement element, int i) {
        WebElement count = element.$(TestBenchElement.class).id("count");
        Assert.assertEquals(String.valueOf(i), count.getText());

        WebElement delta = element.$(TestBenchElement.class).id("delta");
        Assert.assertEquals(Boolean.TRUE.toString(), delta.getText());
    }
}
