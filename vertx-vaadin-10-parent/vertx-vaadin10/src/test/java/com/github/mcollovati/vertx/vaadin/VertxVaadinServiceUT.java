/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import com.github.mcollovati.vertx.utils.MockServletServiceSessionSetup;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.theme.AbstractTheme;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VertxVaadinServiceUT {

    private final String[] es5es6 = new String[] {"es5", "es6"};
    private MockServletServiceSessionSetup mocks;
    private MockServletServiceSessionSetup.TestVertxVaadinService service;

    @Before
    public void setup() throws Exception {
        mocks = new MockServletServiceSessionSetup();
        service = mocks.getService();
    }

    @Test
    public void resolveNullThrows() {
        try {
            service.resolveResource(null, mocks.getBrowser());
            Assert.fail("null should not resolve");
        } catch (NullPointerException e) {
            Assert.assertEquals("Url cannot be null", e.getMessage());
        }
    }

    @Test
    public void resolveResource() {
        Assert.assertEquals("",
            service.resolveResource("", mocks.getBrowser()));
        Assert.assertEquals("foo",
            service.resolveResource("foo", mocks.getBrowser()));
        Assert.assertEquals("/frontend/foo",
            service.resolveResource("frontend://foo", mocks.getBrowser()));
        Assert.assertEquals("/foo",
            service.resolveResource("context://foo", mocks.getBrowser()));
    }

    @Test
    public void resolveResource_production() {
        mocks.setProductionMode(true);

        Assert.assertEquals("",
            service.resolveResource("", mocks.getBrowser()));
        Assert.assertEquals("foo",
            service.resolveResource("foo", mocks.getBrowser()));
        Assert.assertEquals("/frontend-es6/foo",
            service.resolveResource("frontend://foo", mocks.getBrowser()));
        Assert.assertEquals("/foo",
            service.resolveResource("context://foo", mocks.getBrowser()));

        mocks.setBrowserEs6(false);

        Assert.assertEquals("",
            service.resolveResource("", mocks.getBrowser()));
        Assert.assertEquals("foo",
            service.resolveResource("foo", mocks.getBrowser()));
        Assert.assertEquals("/frontend-es5/foo",
            service.resolveResource("frontend://foo", mocks.getBrowser()));
        Assert.assertEquals("/foo",
            service.resolveResource("context://foo", mocks.getBrowser()));
    }

    private void testGetResourceAndGetResourceAsStream(
        String expectedResource, String untranslatedUrl,
        WebBrowser browser, AbstractTheme theme) throws IOException {

        if (expectedResource == null) {
            Assert.assertNull(
                service.getResource(untranslatedUrl, browser, theme));
            Assert.assertNull(service.getResourceAsStream(untranslatedUrl,
                browser, theme));
        } else {
            URL expectedUrl = new URL(
                "file://" + expectedResource);
            URL resource = service.getResource(untranslatedUrl, browser, theme);
            Assert.assertNotNull("Url must not ne null for " + expectedResource, resource);
            Assert.assertThat(resource.toExternalForm(), CoreMatchers.allOf(
                CoreMatchers.startsWith("file:"), CoreMatchers.endsWith(expectedResource)
            ));
            String contents = IOUtils.toString(service.getResourceAsStream(
                untranslatedUrl, browser, theme), StandardCharsets.UTF_8);
            Assert.assertEquals("This is " + expectedResource,
                contents);
        }
    }

    @Test
    public void getResourceNoTheme() throws IOException {
        WebBrowser browser = mocks.getBrowser();
        mocks.getVertxVaadin().addResource("/frontend/foo.txt");
        mocks.getVertxVaadin().addWebJarResource("paper-slider/paper-slider.html");

        testGetResourceAndGetResourceAsStream("/frontend/foo.txt",
            "/frontend/foo.txt", browser, null);
        testGetResourceAndGetResourceAsStream("/frontend/foo.txt",
            "frontend://foo.txt", browser, null);
        testGetResourceAndGetResourceAsStream(null, "frontend://bar.txt",
            browser, null);

        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/paper-slider/paper-slider.html",
            "/frontend/bower_components/paper-slider/paper-slider.html",
            browser, null);
        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/paper-slider/paper-slider.html",
            "frontend://bower_components/paper-slider/paper-slider.html",
            browser, null);
    }

    @Test
    public void getResourceNoTheme_production() throws IOException {
        mocks.getVertxVaadin().addResource("/frontend-es6/foo.txt");
        mocks.getVertxVaadin().addResource("/frontend-es5/foo.txt");

        mocks.setProductionMode(true);
        WebBrowser browser = mocks.getBrowser();

        testGetResourceAndGetResourceAsStream(null, "/frontend/foo.txt",
            browser, null);
        testGetResourceAndGetResourceAsStream("/frontend-es6/foo.txt",
            "frontend://foo.txt", browser, null);
        testGetResourceAndGetResourceAsStream(null, "/frontend/bar.txt",
            browser, null);

        mocks.setBrowserEs6(false);
        testGetResourceAndGetResourceAsStream(null, "/frontend/foo.txt",
            browser, null);
        testGetResourceAndGetResourceAsStream("/frontend-es5/foo.txt",
            "frontend://foo.txt", browser, null);
        testGetResourceAndGetResourceAsStream(null, "/frontend/bar.txt",
            browser, null);
    }

    private final class TestTheme implements AbstractTheme {
        @Override
        public String getBaseUrl() {
            return "/raw/";
        }

        @Override
        public String getThemeUrl() {
            return "/theme/";
        }
    }

    @Test
    public void getResourceTheme() throws IOException {
        WebBrowser browser = mocks.getBrowser();
        TestTheme theme = new TestTheme();

        mocks.getVertxVaadin().addResource("/frontend/raw/raw-only.txt");
        mocks.getVertxVaadin().addResource("/frontend/raw/has-theme-variant.txt");
        mocks.getVertxVaadin().addResource("/frontend/theme/has-theme-variant.txt");
        mocks.getVertxVaadin().addResource("/frontend/theme/theme-only.txt");

        mocks.getVertxVaadin().addWebJarResource("vaadin-button/raw/raw-only.txt");
        mocks.getVertxVaadin().addWebJarResource("vaadin-button/raw/has-theme-variant.txt");
        mocks.getVertxVaadin().addWebJarResource("vaadin-button/theme/has-theme-variant.txt");
        mocks.getVertxVaadin().addWebJarResource("vaadin-button/theme/theme-only.txt");

        // Only raw version
        testGetResourceAndGetResourceAsStream("/frontend/raw/raw-only.txt",
            "frontend://raw/raw-only.txt", browser, theme);
        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/vaadin-button/raw/raw-only.txt",
            "frontend://bower_components/vaadin-button/raw/raw-only.txt",
            browser, theme);
        // Only themed version
        testGetResourceAndGetResourceAsStream("/frontend/theme/theme-only.txt",
            "frontend://raw/theme-only.txt", browser, theme);
        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/vaadin-button/theme/theme-only.txt",
            "frontend://bower_components/vaadin-button/raw/theme-only.txt",
            browser, theme);

        // Raw and themed version
        testGetResourceAndGetResourceAsStream(
            "/frontend/theme/has-theme-variant.txt",
            "frontend://raw/has-theme-variant.txt", browser, theme);
        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/vaadin-button/theme/has-theme-variant.txt",
            "frontend://bower_components/vaadin-button/raw/has-theme-variant.txt",
            browser, theme);
        testGetResourceAndGetResourceAsStream(
            "/frontend/theme/has-theme-variant.txt",
            "frontend://theme/has-theme-variant.txt", browser, null);
        testGetResourceAndGetResourceAsStream(
            "META-INF/resources/webjars/vaadin-button/theme/has-theme-variant.txt",
            "frontend://bower_components/vaadin-button/theme/has-theme-variant.txt",
            browser, theme);
    }

    @Test
    public void getResourceTheme_production() throws IOException, URISyntaxException {
        mocks.setProductionMode(true);
        WebBrowser browser = mocks.getBrowser();
        TestTheme theme = new TestTheme();
        for (String es : es5es6) {
            String frontendFolder = "/frontend-" + es;
            mocks.getVertxVaadin().addResource(
                frontendFolder + "/raw/raw-only.txt");
            mocks.getVertxVaadin().addResource(
                frontendFolder + "/raw/has-theme-variant.txt");
            mocks.getVertxVaadin().addResource(
                frontendFolder + "/theme/has-theme-variant.txt");
            mocks.getVertxVaadin().addResource(
                frontendFolder + "/theme/theme-only.txt");
        }

        for (String es : es5es6) {
            mocks.setBrowserEs6("es6".equals(es));
            //String expectedFrontend = "file:///frontend-" + es;
            String expectedFrontend = Paths.get("frontend-" + es).toUri().toString();
            // Only raw version
            Assert.assertEquals(new URL(expectedFrontend + "/raw/raw-only.txt"),
                service.getResource("frontend://raw/raw-only.txt", browser,
                    theme));

            // Only themed version
            Assert.assertEquals(
                new URL(expectedFrontend + "/theme/theme-only.txt"),
                service.getResource("frontend://raw/theme-only.txt",
                    browser, theme));

            // Raw and themed version
            Assert.assertEquals(
                new URL(expectedFrontend + "/theme/has-theme-variant.txt"),
                service.getResource("frontend://raw/has-theme-variant.txt",
                    browser, theme));
            Assert.assertEquals(
                new URL(expectedFrontend + "/theme/has-theme-variant.txt"),
                service.getResource(
                    "frontend://theme/has-theme-variant.txt", browser,
                    null)); // No theme -> raw version
        }
    }
}
