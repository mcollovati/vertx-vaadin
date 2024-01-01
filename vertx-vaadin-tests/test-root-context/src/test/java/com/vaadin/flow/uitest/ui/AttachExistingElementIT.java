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

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.component.html.testbench.LabelElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AttachExistingElementIT extends ChromeBrowserTest {

    @Test
    public void attachExistingElement() {
        open();

        // attach label
        findElement(By.id("attach-label")).click();

        Assert.assertTrue(isElementPresent(By.id("label")));
        WebElement label = findElement(By.id("label"));
        Assert.assertEquals("label", label.getTagName().toLowerCase(Locale.ENGLISH));

        WebElement parentDiv = findElement(By.id("root-div"));
        List<WebElement> children = parentDiv.findElements(By.xpath("./child::*"));
        boolean labelIsFoundAsChild = false;
        WebElement removeButton = null;
        for (int i = 0; i < children.size(); i++) {
            WebElement child = children.get(i);
            if (child.equals(label)) {
                labelIsFoundAsChild = true;
                WebElement attachButton = children.get(i + 1);
                Assert.assertEquals(
                        "The first inserted component after " + "attached label has wrong index on the client side",
                        "attach-populated-label",
                        attachButton.getAttribute("id"));
                removeButton = children.get(i + 2);
                Assert.assertEquals(
                        "The second inserted component after " + "attached label has wrong index on the client side",
                        "remove-self",
                        removeButton.getAttribute("id"));
                break;
            }
        }

        Assert.assertTrue("The attached label is not found as a child of its parent", labelIsFoundAsChild);

        removeButton.click();
        Assert.assertFalse(isElementPresent(By.id("remove-self")));

        // attach existing server-side element
        findElement(By.id("attach-populated-label")).click();
        Assert.assertEquals("already-populated", label.getAttribute("class"));

        // attach header element
        findElement(By.id("attach-header")).click();

        Assert.assertTrue(isElementPresent(By.id("h1-header")));
        Assert.assertEquals("h1", findElement(By.id("h1-header")).getTagName().toLowerCase(Locale.ENGLISH));

        // attach a child in the shadow root of the div

        findElement(By.id("attach-label-inshadow")).click();
        LabelElement labelInShadow = $(DivElement.class)
                .id("element-with-shadow")
                .$(LabelElement.class)
                .id("label-in-shadow");
        Assert.assertEquals("label", labelInShadow.getTagName().toLowerCase(Locale.ENGLISH));

        // Try to attach non-existing element
        findElement(By.id("non-existing-element")).click();
        Assert.assertTrue(isElementPresent(By.id("non-existing-element")));
    }
}
