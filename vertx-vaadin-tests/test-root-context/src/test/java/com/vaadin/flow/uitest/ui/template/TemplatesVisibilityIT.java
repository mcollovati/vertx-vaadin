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

public class TemplatesVisibilityIT extends ChromeBrowserTest {

    @Test
    public void grandParentVisibility_descendantsAreBound() {
        open();

        TestBenchElement grandParent = $("js-grand-parent").first();
        TestBenchElement subTemplate = grandParent.$("js-sub-template").first();

        // Check child and grand child property values. They shouldn't be set
        // since the elements are not bound
        TestBenchElement subTemplateProp = subTemplate.$(TestBenchElement.class).id("prop");

        TestBenchElement grandChild = subTemplate.$(TestBenchElement.class).id("js-grand-child");

        WebElement grandChildFooProp = grandChild.$(TestBenchElement.class).id("foo-prop");

        WebElement grandChildProp = assertInitialPropertyValues(subTemplateProp, grandChild, grandChildFooProp);

        // make parent visible
        findElement(By.id("grand-parent-visibility")).click();

        // now all descendants should be bound and JS is executed for the
        // grandchild

        assertBound(subTemplateProp, grandChildFooProp, grandChildProp);
    }

    @Test
    public void subTemplateVisibility_grandChildIsBound() {
        open();

        TestBenchElement grandParent = $("js-grand-parent").first();
        TestBenchElement subTemplate = grandParent.$("js-sub-template").first();

        // make sub template invisible
        findElement(By.id("sub-template-visibility")).click();

        // nothing has changed: parent is not bound -> descendants are still not
        // bound
        TestBenchElement subTemplateProp = subTemplate.$(TestBenchElement.class).id("prop");
        TestBenchElement grandChild = subTemplate.$(TestBenchElement.class).id("js-grand-child");
        WebElement grandChildFooProp = grandChild.$(TestBenchElement.class).id("foo-prop");
        assertInitialPropertyValues(subTemplateProp, grandChild, grandChildFooProp);

        // make parent visible
        findElement(By.id("grand-parent-visibility")).click();

        // sub template is invisible now, again: all properties have no values

        WebElement grandChildProp = assertInitialPropertyValues(subTemplateProp, grandChild, grandChildFooProp);

        // make sub template visible
        findElement(By.id("sub-template-visibility")).click();

        // now everything is bound
        assertBound(subTemplateProp, grandChildFooProp, grandChildProp);
    }

    @Test
    public void grandChildVisibility_grandChildIsBound() {
        open();

        TestBenchElement grandParent = $("js-grand-parent").first();
        TestBenchElement subTemplate = grandParent.$("js-sub-template").first();

        // make grand child template invisible
        findElement(By.id("grand-child-visibility")).click();

        // nothing has changed: parent is not bound -> descendants are still not
        // bound
        TestBenchElement subTemplateProp = subTemplate.$(TestBenchElement.class).id("prop");
        TestBenchElement grandChild = subTemplate.$(TestBenchElement.class).id("js-grand-child");
        WebElement grandChildFooProp = grandChild.$(TestBenchElement.class).id("foo-prop");
        assertInitialPropertyValues(subTemplateProp, grandChild, grandChildFooProp);

        // make grand parent visible
        findElement(By.id("grand-parent-visibility")).click();

        // grand child template is invisible now, again: all its properties have
        // no values

        WebElement grandChildProp = grandChild.$(TestBenchElement.class).id("prop");
        Assert.assertNotEquals("bar", grandChildFooProp.getText());
        Assert.assertNotEquals("foo", grandChildProp.getText());

        // make grand child template visible
        findElement(By.id("grand-child-visibility")).click();

        // now everything is bound
        assertBound(subTemplateProp, grandChildFooProp, grandChildProp);
    }

    @Test
    public void invisibleComponent_dropClientSideChanges() {
        open();

        // make parent visible
        findElement(By.id("grand-parent-visibility")).click();

        TestBenchElement grandParent = $("js-grand-parent").first();
        TestBenchElement subTemplate = grandParent.$("js-sub-template").first();

        WebElement subTemplateProp = subTemplate.$(TestBenchElement.class).id("prop");

        Assert.assertEquals("bar", subTemplateProp.getText());

        // make sub template invisible
        findElement(By.id("sub-template-visibility")).click();

        // change the sub template property via client side
        findElement(By.id("client-side-update-property")).click();

        // The property value has not changed
        Assert.assertEquals("bar", subTemplateProp.getText());

        // make template visible
        findElement(By.id("sub-template-visibility")).click();

        // One more check : the property value is still the same
        Assert.assertEquals("bar", subTemplateProp.getText());

        // change the sub template property via client side one more time
        // (now the component is visible)
        findElement(By.id("client-side-update-property")).click();

        // Now the property value should be changed
        Assert.assertEquals("baz", subTemplateProp.getText());
    }

    private WebElement assertInitialPropertyValues(
            TestBenchElement subTemplateProp, TestBenchElement grandChild, WebElement grandChildFooProp) {
        WebElement grandChildProp = grandChild.$(TestBenchElement.class).id("prop");

        Assert.assertNotEquals("bar", subTemplateProp.getText());

        Assert.assertNotEquals("bar", grandChildFooProp.getText());

        Assert.assertNotEquals("foo", grandChildProp.getText());
        return grandChildProp;
    }

    private void assertBound(WebElement subTemplateProp, WebElement grandChildFooProp, WebElement grandChildProp) {
        waitUntil(driver -> "bar".equals(subTemplateProp.getText()));
        // This is the result of JS execution
        waitUntil(driver -> "bar".equals(grandChildFooProp.getText()));
        // This is the value for the grand child received from the server side
        Assert.assertEquals("foo", grandChildProp.getText());
    }
}
