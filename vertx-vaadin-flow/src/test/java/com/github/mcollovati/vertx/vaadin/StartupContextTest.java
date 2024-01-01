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

import java.util.Set;
import java.util.function.Predicate;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.github.mcollovati.vertx.support.StartupContext;

import static org.assertj.core.api.Assertions.assertThat;

public class StartupContextTest {

    public static final String META_INF_RESOURCES = "META-INF/resources/";
    private StartupContext startupContext;

    @Before
    public void setup() {
        JsonObject config = new JsonObject();
        config.put("debug", System.getProperty("vertxvaadin.classgraph.debug", "false"));
        startupContext = StartupContext.syncOf(Vertx.vertx(), new VaadinOptions(config));
    }

    @Test
    public void testGetResourcePaths() {
        Set<String> resourcePaths = startupContext.servletContext().getResourcePaths("/");
        assertThat(resourcePaths).isNotEmpty().allMatch(isChildOf(""));
        System.out.println(resourcePaths);

        resourcePaths = startupContext.servletContext().getResourcePaths("");
        assertThat(resourcePaths).isNotEmpty().allMatch(isChildOf(""));
        System.out.println(resourcePaths);

        resourcePaths = startupContext.servletContext().getResourcePaths("webjars");
        assertThat(resourcePaths).isNotEmpty().allMatch(isChildOf("webjars/"));
        System.out.println(resourcePaths);
    }

    @Test
    public void testResolveResource() {
        assertThat(startupContext.resolveResource("VAADIN/static/push/vaadinPushSockJS.js.gz"))
                .hasValue("META-INF/resources/VAADIN/static/push/vaadinPushSockJS.js.gz");
        assertThat(startupContext.resolveResource("/VAADIN/static/push/vaadinPushSockJS.js.gz"))
                .hasValue("META-INF/resources/VAADIN/static/push/vaadinPushSockJS.js.gz");
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
