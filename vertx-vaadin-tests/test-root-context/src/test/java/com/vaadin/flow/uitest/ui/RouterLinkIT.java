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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;

public class RouterLinkIT extends ChromeBrowserTest {

    @Test
    @Ignore("Ignored because of issue in fusion : https://github.com/vaadin/flow/issues/7575")
    public void testRoutingLinks_insideServletMapping_updateLocation() {
        open();

        verifySamePage();

        testInsideServlet("foo", "foo", "", "foo");
        testInsideServlet("./foobar", "foobar", "", "foobar");
        testInsideServlet("foo/bar", "foo/bar", "", "foo/bar");

        testInsideServlet("./foobar?what=not", "foobar", "what=not", "foobar?what=not");

        testInsideServlet("/view/baz", "baz", "", "baz");

        testInsideServlet("./foobar?what=not#fragment", "foobar", "what=not", "foobar?what=not#fragment");

        clickLink("empty");
        verifyInsideServletLocation("");
        verifySamePage();
    }

    @Test
    public void testRoutingLinks_outsideServletMapping_pageChanges() {
        open();

        verifySamePage();

        clickLink("/run");
        verifyNotSamePage();
    }

    @Test
    public void testRoutingLinks_externalLink_pageChanges() {
        open();

        verifySamePage();

        clickLink("http://example.net/");

        String currentUrl = getDriver().getCurrentUrl();

        // Chrome changes url to whatever it can, removing www part, forcing
        // https.
        Assert.assertTrue("Invalid URL: " + currentUrl, currentUrl.equals("http://example.net/"));
    }

    @Test
    @Ignore("Ignored because of issue in fusion : https://github.com/vaadin/flow/issues/7575")
    public void testImageInsideRouterLink() {
        open();

        verifySamePage();

        findElement(By.tagName("img")).click();

        verifyInsideServletLocation("image/link");

        verifyPopStateEvent("image/link");
    }

    private void testInsideServlet(
            String linkToTest, String popStateLocation, String parametersQuery, String pathAfterServletMapping) {
        clickLink(linkToTest);
        verifyInsideServletLocation(pathAfterServletMapping);
        verifyParametersQuery(parametersQuery);
        verifyPopStateEvent(popStateLocation);
        verifySamePage();
    }

    private void clickLink(String linkText) {
        findElement(By.linkText(linkText)).click();
    }

    private void verifyInsideServletLocation(String pathAfterServletMapping) {
        Assert.assertEquals(
                "Invalid URL",
                getRootURL() + "/view/" + pathAfterServletMapping,
                getDriver().getCurrentUrl());
    }

    private void verifyParametersQuery(String parametersQuery) {
        Assert.assertEquals(
                "Invalid server side event location",
                parametersQuery,
                findElement(By.id("queryParams")).getText());
    }

    private void verifyPopStateEvent(String location) {
        Assert.assertEquals(
                "Invalid server side event location",
                location,
                findElement(By.id("location")).getText());
    }

    private void verifyNotSamePage() {
        Assert.assertEquals(0, findElements(By.id("location")).size());
    }

    private void verifySamePage() {
        Assert.assertNotNull("Page has changed", findElement(By.id("location")));
    }
}
