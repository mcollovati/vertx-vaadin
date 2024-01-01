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

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class RequestParametersHistoryIT extends ChromeBrowserTest {
    @Test
    public void noParameters() {
        open();
        WebElement label = findElement(By.id(RequestParametersHistoryView.REQUEST_PARAM_ID));

        Assert.assertEquals(RequestParametersHistoryView.NO_INPUT_TEXT, label.getText());
    }

    @Test
    public void parameterProvided() {
        String paramValue = "Super-intelligent shade of the colour blue";
        open(String.format("%s=%s", RequestParametersHistoryView.REQUEST_PARAM_NAME, paramValue));
        WebElement label = findElement(By.id(RequestParametersHistoryView.REQUEST_PARAM_ID));
        Assert.assertEquals(paramValue, label.getText());
    }

    @Test
    public void goBachToHistoryWithParameters() {
        String oldParamValue = "oldParamValue";
        String newParamValue = "newParamValue";
        open(String.format("%s=%s", RequestParametersHistoryView.REQUEST_PARAM_NAME, oldParamValue));
        open(String.format("%s=%s", RequestParametersHistoryView.REQUEST_PARAM_NAME, newParamValue));

        findElement(By.id(RequestParametersHistoryView.BACK_BUTTON_ID)).click();
        waitForElementPresent(By.id(RequestParametersHistoryView.REQUEST_PARAM_ID));

        WebElement label = findElement(By.id(RequestParametersHistoryView.REQUEST_PARAM_ID));

        Assert.assertEquals(oldParamValue, label.getText());
    }
}
