package com.github.mcollovati.vertx.vaadin;

import java.util.Set;
import java.util.function.Predicate;

import com.github.mcollovati.vertx.support.StartupContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupContextTest {

    public static final String META_INF_RESOURCES = "META-INF/resources/";
    private StartupContext startupContext;

    @Before
    public void setup() {
        JsonObject config = new JsonObject();
        config.put("debug", true);
        startupContext = StartupContext.syncOf(Vertx.vertx(), new VaadinOptions(config));
    }

    @Test
    public void testGetResourcePaths() {
        Set<String> resourcePaths = startupContext.servletContext().getResourcePaths("/");
        assertThat(resourcePaths).isNotEmpty()
            .allMatch(isChildOf(""));
        System.out.println(resourcePaths);

        resourcePaths = startupContext.servletContext().getResourcePaths("");
        assertThat(resourcePaths).isNotEmpty()
            .allMatch(isChildOf(""));
        System.out.println(resourcePaths);

        resourcePaths = startupContext.servletContext().getResourcePaths("webjars");
        assertThat(resourcePaths).isNotEmpty()
            .allMatch(isChildOf("webjars/"));
        System.out.println(resourcePaths);
    }

    @Test
    public void testResolveResource() {
        assertThat(startupContext.resolveResource("VAADIN/static/push/vaadinPushSockJS.js.gz"))
            .hasValue("META-INF/resources/VAADIN/static/push/vaadinPushSockJS.js.gz");
        assertThat(startupContext.resolveResource("/VAADIN/static/push/vaadinPushSockJS.js.gz"))
            .hasValue("META-INF/resources/VAADIN/static/push/vaadinPushSockJS.js.gz");
        assertThat(startupContext.resolveResource("vertx-version.txt"))
            .hasValue("vertx-version.txt");
        assertThat(startupContext.resolveResource("/vertx-version.txt"))
            .hasValue("vertx-version.txt");
    }

    private Predicate<String> isChildOf(String parent) {
        return path -> {
            if (path.startsWith(META_INF_RESOURCES)) {
                path = path.substring(META_INF_RESOURCES.length());
            }
            path = path.substring(parent.length());
            return path.chars().filter(i -> i == '/').count() <= 1;
        };
    }
}
