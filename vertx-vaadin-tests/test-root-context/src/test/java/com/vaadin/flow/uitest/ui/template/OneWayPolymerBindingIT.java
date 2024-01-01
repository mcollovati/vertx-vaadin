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

import static org.junit.Assert.assertTrue;

/**
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class OneWayPolymerBindingIT extends ChromeBrowserTest {

    // Numerous tests are carried out in the single test case, because it's
    // expensive to launch numerous Chrome instances
    @Test
    public void initialModelValueIsPresentAndModelUpdatesNormally() {
        open();

        TestBenchElement template = $(TestBenchElement.class).id("template");

        checkInitialState(template);
        checkTemplateModel(template);

        template.$(TestBenchElement.class).id("changeModelValue").click();

        checkStateAfterClick(template);
        checkTemplateModel(template);
    }

    private void checkInitialState(TestBenchElement template) {
        String messageDivText =
                template.$(TestBenchElement.class).id("messageDiv").getText();
        String titleDivText = template.$(TestBenchElement.class).id("titleDiv").getText();
        Assert.assertEquals(OneWayPolymerBindingView.MESSAGE, messageDivText);
        Assert.assertEquals("", titleDivText);
    }

    private void checkTemplateModel(TestBenchElement template) {
        assertTrue(template.$(TestBenchElement.class)
                        .attribute("id", "titleDivConditional")
                        .all()
                        .size()
                > 0);
        Assert.assertEquals(
                0,
                template.$(TestBenchElement.class)
                        .attribute("id", "nonExistingProperty")
                        .all()
                        .size());
    }

    private void checkStateAfterClick(TestBenchElement template) {
        String changedMessageDivText =
                template.$(TestBenchElement.class).id("messageDiv").getText();
        String titleDivText = template.$(TestBenchElement.class).id("titleDiv").getText();

        Assert.assertEquals(OneWayPolymerBindingView.NEW_MESSAGE, changedMessageDivText);
        Assert.assertEquals("", titleDivText);
    }
}
