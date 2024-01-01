/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.Constants;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.RoutingContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.mcollovati.vertx.utils.MockServiceSessionSetup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VertxVaadinServiceUT {

    private final String[] es5es6 = new String[] {"es5", "es6"};
    private MockServiceSessionSetup mocks;
    private MockServiceSessionSetup.TestVertxVaadinService service;

    @Before
    public void setUp() throws Exception {
        mocks = new MockServiceSessionSetup();
        service = mocks.getService();
        service.init();
    }

    @Test
    public void resolveNullThrows() {
        try {
            service.resolveResource(null);
            Assert.fail("null should not resolve");
        } catch (NullPointerException e) {
            Assert.assertEquals("Url cannot be null", e.getMessage());
        }
    }

    @Test
    public void resolveResource() {
        Assert.assertEquals("", service.resolveResource(""));
        Assert.assertEquals("foo", service.resolveResource("foo"));
        Assert.assertEquals("/foo", service.resolveResource("context://foo"));
    }

    @Test
    public void resolveResourceNPM_production() {
        mocks.setProductionMode(true);

        Assert.assertEquals("", service.resolveResource(""));
        Assert.assertEquals("foo", service.resolveResource("foo"));
        Assert.assertEquals("/foo", service.resolveResource("context://foo"));
    }

    @Test
    public void should_report_flow_bootstrapHandler() {
        mocks.getDeploymentConfiguration().useDeprecatedV14Bootstrapping(true);

        Assert.assertTrue(UsageStatistics.getEntries()
                .anyMatch(e -> Constants.STATISTIC_FLOW_BOOTSTRAPHANDLER.equals(e.getName())));
    }

    private String testLocation(String base, String contextPath, String pathInfo) throws Exception {

        RoutingContext routingContext = createRequest(base, contextPath, pathInfo);

        VertxVaadinService service = mock(VertxVaadinService.class);
        Mockito.doCallRealMethod().when(service).getContextRootRelativePath(Mockito.any());

        VertxVaadinRequest request = new VertxVaadinRequest(service, routingContext);
        String location = service.getContextRootRelativePath(request);
        return location;
    }

    /**
     * Creates a HttpServletRequest mock using the supplied parameters.
     *
     * @param base        The base url, e.g. http://localhost:8080
     * @param contextPath The context path where the application is deployed, e.g.
     *                    /mycontext
     * @param servletPath The servlet path to the servlet we are testing, e.g. /myapp
     * @param pathInfo    Any text following the servlet path in the request, not
     *                    including query parameters, e.g. /UIDL/
     * @return A mock HttpServletRequest object useful for testing
     * @throws MalformedURLException
     */
    private RoutingContext createRequest(String base, String contextPath, String pathInfo)
            throws MalformedURLException {
        URL url = new URL(base + contextPath + pathInfo);
        RoutingContext routingContext = mock(RoutingContext.class);
        HttpServerRequest request = mock(HttpServerRequest.class);
        SocketAddress address = new SocketAddressImpl(url.getPort() >= 0 ? url.getPort() : 80, url.getHost());
        when(routingContext.request()).thenReturn(request);
        when(request.isSSL()).thenReturn(url.getProtocol().equalsIgnoreCase("https"));
        when(request.remoteAddress()).thenReturn(address);
        when(request.uri()).thenReturn(url.getPath());
        when(routingContext.mountPoint()).thenReturn(contextPath);
        when(request.path()).thenReturn(pathInfo);
        mocks.getSession().getSession();

        return routingContext;
    }
}
