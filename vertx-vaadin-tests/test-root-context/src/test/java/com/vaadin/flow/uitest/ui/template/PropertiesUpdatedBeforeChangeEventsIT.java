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
package com.vaadin.flow.uitest.ui.template;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class PropertiesUpdatedBeforeChangeEventsIT extends ChromeBrowserTest {

    private WebElement firstPropInput;
    private WebElement secondPropDiv;
    private WebElement serverSetTextDiv;

    @Before
    public void init() {
        open();
        firstPropInput = getElementById("first-prop-input");
        secondPropDiv = getElementById("second-prop-div");
        serverSetTextDiv = getElementById("text-div");
    }

    @Test
    public void all_properties_update_before_change_event_handlers_are_called() {
        assertTextsCorrect("");
        String textToSet = "abcdefg";
        firstPropInput.sendKeys(textToSet);
        assertTextsCorrect(textToSet);
    }

    private void assertTextsCorrect(String expected) {
        Assert.assertEquals(expected, secondPropDiv.getText());
        Assert.assertEquals(secondPropDiv.getText(), serverSetTextDiv.getText());
    }

    private WebElement getElementById(String id) {
        return $("properties-updated-before-change-events")
                .first()
                .$(TestBenchElement.class)
                .id(id);
    }
}
