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

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class VaadinVerticleWidgetsetUT {

    public static final String TEST_WIDGETSET = "MyWidgetSet";
    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void shouldIgnoreDefaultWidgetsetFromVaadinServletConfiguration(TestContext context) {
        WithoutDefaultWidgetSet verticle = new WithoutDefaultWidgetSet();
        rule.vertx().deployVerticle(verticle, context.asyncAssertSuccess(event -> {
            assertThat(verticle.config.containsKey(VaadinServlet.PARAMETER_WIDGETSET)).isFalse();
        }));
    }

    @Test
    public void shouldUseWidgetsetFromVaadinServletConfiguration(TestContext context) {
        WithDefaultWidgetSet verticle = new WithDefaultWidgetSet();
        rule.vertx().deployVerticle(verticle, context.asyncAssertSuccess(event -> {
            assertThat(verticle.config.containsKey(VaadinServlet.PARAMETER_WIDGETSET)).isTrue();
            assertThat(verticle.config.getString(VaadinServlet.PARAMETER_WIDGETSET)).isEqualTo(TEST_WIDGETSET);
        }));
    }

    private static abstract class StubVerticle extends VaadinVerticle {
        JsonObject config;

        @Override
        protected VertxVaadin createVertxVaadin(JsonObject vaadinConfig) {
            this.config = vaadinConfig;
            return super.createVertxVaadin(vaadinConfig);
        }
    }

    @VaadinServletConfiguration(ui = UI.class, productionMode = false, widgetset = TEST_WIDGETSET)
    private static class WithDefaultWidgetSet extends StubVerticle {
    }

    @VaadinServletConfiguration(ui = UI.class, productionMode = false)
    private static class WithoutDefaultWidgetSet extends StubVerticle {
    }


}
