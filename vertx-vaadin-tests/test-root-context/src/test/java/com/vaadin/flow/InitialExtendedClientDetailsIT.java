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
package com.vaadin.flow;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;

public class InitialExtendedClientDetailsIT extends ChromeBrowserTest {

    private final TypeSafeMatcher<String> isParseableAsInteger() {
        return new TypeSafeMatcher<String>() {

            @Override
            protected boolean matchesSafely(String s) {
                try {
                    Integer.parseInt(s);
                    return true;
                } catch (NumberFormatException nfe) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("only digits");
            }
        };
    }

    @Test
    public void verifyClientDetails() {
        open();

        Assert.assertThat(findElement(By.id("screenWidth")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("screenHeight")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("windowInnerWidth")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("windowInnerHeight")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("bodyClientWidth")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("bodyClientHeight")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("timezoneOffset")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("timeZoneId")).getText(), not(isEmptyString()));
        Assert.assertThat(findElement(By.id("rawTimezoneOffset")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("DSTSavings")).getText(), isParseableAsInteger());
        Assert.assertThat(findElement(By.id("DSTInEffect")).getText(), isOneOf("true", "false"));
        Assert.assertThat(findElement(By.id("currentDate")).getText(), not(isEmptyString()));
        Assert.assertThat(findElement(By.id("touchDevice")).getText(), isOneOf("true", "false"));
        Assert.assertThat(findElement(By.id("windowName")).getText(), not(isEmptyString()));
    }
}
