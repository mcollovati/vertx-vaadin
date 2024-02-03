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

import com.vaadin.flow.component.html.testbench.InputTextElement;
import com.vaadin.flow.component.html.testbench.NativeButtonElement;
import com.vaadin.flow.component.html.testbench.SpanElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.viteapp.views.template.LitComponent;
import com.vaadin.viteapp.views.template.PolymerComponent;
import com.vaadin.viteapp.views.template.TemplateView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.example.addon.AddonLitComponent;

public class TemplateIT extends ChromeBrowserTest {
    @Before
    public void openView() {
        getDriver().get(getRootURL() + "/" + TemplateView.ROUTE);
        waitForDevServer();
        getCommandExecutor().waitForVaadin();
    }

    @Test
    public void testElementIdMapping() {
        final String initialValue = "Default";

        SpanElement litSpan = $(LitComponent.TAG).first().$(SpanElement.class).first();
        Assert.assertEquals(initialValue, litSpan.getText());

        SpanElement polymerSpan =
                $(PolymerComponent.TAG).first().$(SpanElement.class).first();
        Assert.assertEquals(initialValue, polymerSpan.getText());

        SpanElement addonLitSpan =
                $(AddonLitComponent.TAG).first().$(SpanElement.class).first();
        Assert.assertEquals(initialValue, addonLitSpan.getText());

        final String newLabel = "New label";
        $(InputTextElement.class).first().setValue(newLabel);
        $(NativeButtonElement.class).first().click();

        Assert.assertEquals(newLabel, litSpan.getText());
        Assert.assertEquals(newLabel, polymerSpan.getText());
        Assert.assertEquals(newLabel, addonLitSpan.getText());
    }
}
