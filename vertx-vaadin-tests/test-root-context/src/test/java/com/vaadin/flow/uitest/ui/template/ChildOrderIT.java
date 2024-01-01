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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Tests to validate the ordering of server-side nodes when added alongside
 * client-side nodes.
 *
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class ChildOrderIT extends ChromeBrowserTest {

    private TestBenchElement root;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.id("root"));
        root = $(TestBenchElement.class).id("root");
    }

    @Test
    public void appendElementsFromServer_elementsAreAddedAfterExistingOnes() {
        TestBenchElement container = root.$(TestBenchElement.class).id("containerWithElement");

        assertNodeOrder(container, "Client child");

        clickAndWaitForContainerToChange(container, "addChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 1");

        clickAndWaitForContainerToChange(container, "addChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 1", "Server child 2");

        clickAndWaitForContainerToChange(container, "addClientSideChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 1", "Server child 2", "Client child");

        /*
         * Client side nodes added after the server side ones are not considered
         * in the counting, so they are left behind
         */
        clickAndWaitForContainerToChange(container, "addChildToContainer1");
        assertNodeOrder(
                container, "Client child", "Server child 1", "Server child 2", "Server child 3", "Client child");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer1");
        assertNodeOrder(container, "Client child", "Server child 1", "Server child 2", "Client child");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer1");
        assertNodeOrder(container, "Client child", "Server child 1", "Client child");
    }

    @Test
    public void prependElementsFromServer_elementsAreAddedBeforeExistingOnes() {
        TestBenchElement container = root.$(TestBenchElement.class).id("containerWithElement");

        assertNodeOrder(container, "Client child");

        clickAndWaitForContainerToChange(container, "prependChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 1");

        clickAndWaitForContainerToChange(container, "prependChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 2", "Server child 1");

        clickAndWaitForContainerToChange(container, "addClientSideChildToContainer1");
        assertNodeOrder(container, "Client child", "Server child 2", "Server child 1", "Client child");

        clickAndWaitForContainerToChange(container, "prependChildToContainer1");
        assertNodeOrder(
                container, "Client child", "Server child 3", "Server child 2", "Server child 1", "Client child");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer1");
        assertNodeOrder(container, "Client child", "Server child 3", "Server child 2", "Client child");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer1");
        assertNodeOrder(container, "Client child", "Server child 3", "Client child");
    }

    @Test
    public void appendTextsFromServer_textsAreAddedAfterExistingOnes() {
        TestBenchElement container = root.$(TestBenchElement.class).id("containerWithText");

        assertNodeOrder(container, "Client text");

        clickAndWaitForContainerToChange(container, "addChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 1");

        clickAndWaitForContainerToChange(container, "addChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 1", "Server text 2");

        clickAndWaitForContainerToChange(container, "addClientSideChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 1", "Server text 2", "Client text");

        /*
         * Client side nodes added after the server side ones are not considered
         * in the counting, so they are left behind
         */
        clickAndWaitForContainerToChange(container, "addChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 1", "Server text 2", "Server text 3", "Client text");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer2");
        assertNodeOrder(container, "Client text", "Server text 1", "Server text 2", "Client text");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer2");
        assertNodeOrder(container, "Client text", "Server text 1", "Client text");
    }

    @Test
    public void prependTextsFromServer_textsAreAddedBeforeExistingOnes() {
        TestBenchElement container = root.$(TestBenchElement.class).id("containerWithText");

        assertNodeOrder(container, "Client text");

        clickAndWaitForContainerToChange(container, "prependChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 1");

        clickAndWaitForContainerToChange(container, "prependChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 2", "Server text 1");

        clickAndWaitForContainerToChange(container, "addClientSideChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 2", "Server text 1", "Client text");

        clickAndWaitForContainerToChange(container, "prependChildToContainer2");
        assertNodeOrder(container, "Client text", "Server text 3", "Server text 2", "Server text 1", "Client text");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer2");
        assertNodeOrder(container, "Client text", "Server text 3", "Server text 2", "Client text");

        clickAndWaitForContainerToChange(container, "removeChildFromContainer2");
        assertNodeOrder(container, "Client text", "Server text 3", "Client text");
    }

    @Test
    public void containerWithElementAddedOnConstructor_orderIsPreserved() {
        TestBenchElement container = root.$(TestBenchElement.class).id("containerWithElementAddedOnConstructor");

        assertNodeOrder(container, "Client child", "Server child 1", "Server child 2");
    }

    private void clickAndWaitForContainerToChange(WebElement container, String buttonToclick) {
        String innertText = container.getAttribute("innerText");
        TestBenchElement button = root.$(TestBenchElement.class).id(buttonToclick);
        button.click();
        waitUntilNot(driver -> container.getAttribute("innerText").equals(innertText));
    }

    private void assertNodeOrder(WebElement container, String... nodes) {
        String texts = container.getText();
        texts = texts.replace("\n", " ");
        String expected = Stream.of(nodes).collect(Collectors.joining(" "));
        Assert.assertEquals(expected, texts);
    }
}
