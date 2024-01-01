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
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.uitest.ui.AbstractErrorIT;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Test;

public class ExceptionsDuringPropertyUpdatesIT extends AbstractErrorIT {

    @Test
    public void exceptionInMapSyncDoesNotCauseInternalError() {
        open();

        TestBenchElement template = $("exceptions-property-update").first();

        template.$("button").id("set-properties").click();

        assertNoSystemErrors();

        TestBenchElement msg = template.$("div").id("message");

        Assert.assertEquals("Name is updated to bar", msg.getText());

        List<TestBenchElement> errors =
                template.$("div").attribute("class", "error").all();

        Set<String> errorMsgs = errors.stream().map(TestBenchElement::getText).collect(Collectors.toSet());

        Assert.assertEquals(2, errorMsgs.size());

        Assert.assertTrue(
                errorMsgs.contains(
                        "An error occurred: java.lang.RuntimeException: Intentional exception in property sync handler for 'text'"));
        Assert.assertTrue(
                errorMsgs.contains(
                        "An error occurred: java.lang.IllegalStateException: Intentional exception in property sync handler for 'title'"));
    }
}
