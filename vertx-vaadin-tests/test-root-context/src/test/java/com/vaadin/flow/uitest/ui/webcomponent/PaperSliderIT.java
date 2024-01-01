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
package com.vaadin.flow.uitest.ui.webcomponent;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;

public class PaperSliderIT extends ChromeBrowserTest {

    @Test
    public void domCorrect() {
        open();

        WebElement eventField = findElement(By.id(PaperSliderView.VALUE_TEXT_ID));
        Assert.assertNotNull("No text value found on the page", eventField);

        WebElement paperSlider = findElement(By.tagName("paper-slider"));
        Assert.assertNotNull("No slider found on the page", paperSlider);

        int initialValue = PaperSliderView.INITIAL_VALUE;

        assertSliderValue(paperSlider, initialValue);

        changeSliderValueViaApi(eventField, paperSlider, initialValue + 1);
        changeSliderValueViaButton(eventField, paperSlider, PaperSliderView.UPDATED_VALUE);
    }

    private void changeSliderValueViaApi(WebElement eventField, WebElement paperSlider, int expectedValue) {
        executeScript("arguments[0].increment()", paperSlider);
        assertSliderValue(paperSlider, expectedValue);
        assertEventFieldValue(eventField, expectedValue);
    }

    private void changeSliderValueViaButton(WebElement eventField, WebElement paperSlider, int expectedValue) {
        findElement(By.id(PaperSliderView.CHANGE_VALUE_ID)).click();
        assertSliderValue(paperSlider, expectedValue);
        assertEventFieldValue(eventField, expectedValue);
    }

    private static void assertSliderValue(WebElement paperSlider, int expectedValue) {
        Assert.assertThat(
                "Slider has incorrect value", Integer.valueOf(paperSlider.getAttribute("value")), is(expectedValue));
    }

    private static void assertEventFieldValue(WebElement eventField, int expectedValue) {
        Assert.assertThat(
                "Expected event field to be updated after slider value was changed",
                eventField.getText(),
                is(String.format("Value: %s (set on client)", expectedValue)));
    }
}
