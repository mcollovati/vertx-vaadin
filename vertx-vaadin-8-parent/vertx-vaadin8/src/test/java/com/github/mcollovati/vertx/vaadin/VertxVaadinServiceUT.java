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

import java.util.Optional;

import com.vaadin.server.VaadinRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VertxVaadinServiceUT {


    @Test
    public void testGetContextRootRelativePath() {

        // http://dummy.host:8080/contextpath/servlet
        // should return . (relative url resolving to /contextpath)
        assertThat(testLocation("/contextPath", "/servlet")).isEqualTo(".");

        // http://dummy.host:8080/contextpath/servlet/
        // should return ./.. (relative url resolving to /contextpath)
        assertThat(testLocation("/contextPath", "/servlet/")).isEqualTo("./..");

        // http://dummy.host:8080/servlet
        // should return "."
        assertThat(testLocation(null, "/servlet")).isEqualTo(".");

        // http://dummy.host/contextpath/servlet/extra/stuff
        // should return ./../.. (relative url resolving to /contextpath)
        assertThat(testLocation("/contextpath", "/servlet/extra/stuff"))
            .isEqualTo("./../..");

        // http://dummy.host/contextpath/servlet/extra/stuff/
        // should return ./../../.. (relative url resolving to /contextpath)
        assertThat(testLocation("/contextpath", "/servlet/extra/stuff/"))
            .isEqualTo("./../../..");

        // http://dummy.host/context/path/servlet/extra/stuff
        // should return ./../.. (relative url resolving to /context/path)
        assertThat(testLocation("/context/path", "/servlet/extra/stuff"))
            .isEqualTo("./../..");

        // http://dummy.host:8080/contextpath
        // should return . (relative url resolving to /contextpath)
        assertThat(testLocation("/contextPath", "")).isEqualTo(".");

    }

    private String testLocation(String mountPoint, String pathInfo) {
        VertxVaadinService service = mock(VertxVaadinService.class);
        HttpServerRequest httpServerRequest = mock(HttpServerRequest.class);

        RoutingContext routingContext = mock(RoutingContext.class);
        when(routingContext.mountPoint()).thenReturn(mountPoint);
        when(routingContext.request()).thenReturn(httpServerRequest);

        VaadinRequest req = new VertxVaadinRequest(service, routingContext);

        when(httpServerRequest.path()).thenReturn(Optional.ofNullable(mountPoint).orElse("") + pathInfo);
        return VertxVaadinService.getContextRootRelativePath(req);
    }

}
