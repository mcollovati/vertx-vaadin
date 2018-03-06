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
