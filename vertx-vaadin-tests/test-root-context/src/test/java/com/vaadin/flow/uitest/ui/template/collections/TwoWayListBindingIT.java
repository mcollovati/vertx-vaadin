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
package com.vaadin.flow.uitest.ui.template.collections;

import java.util.List;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

public class TwoWayListBindingIT extends ChromeBrowserTest {

    @Test
    public void itemsInList_twoWayDataBinding_updatesAreSentToServer() {
        open();

        findElement(By.id("enable")).click();

        List<WebElement> fields =
                $("two-way-list-binding").first().$(DivElement.class).first().findElements(By.id("input"));

        // self check
        Assert.assertEquals(2, fields.size());

        fields.get(0).clear();
        fields.get(0).sendKeys("baz");
        fields.get(0).sendKeys(Keys.TAB);

        Assert.assertEquals("[baz, bar]", getLastInfoMessage());

        fields.get(1).sendKeys("foo");
        fields.get(1).sendKeys(Keys.TAB);

        Assert.assertEquals("[baz, barfoo]", getLastInfoMessage());
    }

    private String getLastInfoMessage() {
        List<WebElement> messages = findElements(By.className("messages"));
        // self check
        Assert.assertTrue(messages.size() > 0);
        return messages.get(messages.size() - 1).getText();
    }
}
