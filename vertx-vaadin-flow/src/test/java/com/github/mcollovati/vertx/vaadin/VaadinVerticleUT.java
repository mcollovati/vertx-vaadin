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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletException;

import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.InitParameters;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.mcollovati.failures.lookup.FakeLookupInitializer;

@RunWith(VertxUnitRunner.class)
public class VaadinVerticleUT {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void startWithPromiseShouldInvokeSynStart(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        DeploymentOptions opts = new DeploymentOptions().setConfig(testConfig());
        rule.vertx()
                .deployVerticle(
                        verticle,
                        opts,
                        context.asyncAssertSuccess(
                                ev -> context.assertTrue(verticle.startInvoked.get(), "Start not invoked")));
    }

    @Test
    public void stopWithPromiseShouldInvokeSynStart(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        DeploymentOptions opts = new DeploymentOptions().setConfig(testConfig());
        rule.vertx().deployVerticle(verticle, opts, context.asyncAssertSuccess(ev -> rule.vertx()
                .undeploy(
                        ev,
                        context.asyncAssertSuccess(
                                ev2 -> context.assertTrue(verticle.stopInvoked.get(), "Stop not invoked")))));
    }

    @Test
    public void shouldPropagateFailureIfInitializationFails(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        JsonObject config = testConfig();
        config.getJsonObject("vaadin")
                .getJsonArray("flowBasePackages")
                .add(FakeLookupInitializer.class.getPackage().getName());
        DeploymentOptions opts = new DeploymentOptions().setConfig(config);
        rule.vertx().deployVerticle(verticle, opts, context.asyncAssertFailure(ev -> Assertions.assertThat(ev)
                .isExactlyInstanceOf(ServletException.class)));
    }

    @Test
    public void shouldDisableHillaSupportByConfiguration(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        JsonObject config = testConfig();
        DeploymentOptions opts = new DeploymentOptions().setConfig(config);
        rule.vertx()
                .deployVerticle(
                        verticle,
                        opts,
                        context.asyncAssertSuccess(ev -> context.assertFalse(
                                verticle.service.getVaadinOptions().hillaEnabled(),
                                "Hilla support should be disabled")));
    }

    @Test
    public void shouldFindI18NSupport(TestContext context) {
        TestVerticle verticle = new TestVerticle() {
            @Override
            public JsonObject config() {
                JsonObject config = super.config();
                if (!config.containsKey("vaadin")) {
                    config.put("vaadin", new JsonObject());
                }
                JsonObject vaadin = config.getJsonObject("vaadin");
                vaadin.put(
                        "flowBasePackages",
                        new JsonArray().add(getClass().getPackage().getName()));
                if (!vaadin.containsKey(InitParameters.I18N_PROVIDER)) {
                    vaadin.put(InitParameters.I18N_PROVIDER, TestI18nProvider.class.getName());
                }

                return config;
            }
        };
        DeploymentOptions opts = new DeploymentOptions();
        rule.vertx()
                .deployVerticle(
                        verticle,
                        opts,
                        context.asyncAssertSuccess(ev -> context.assertTrue(
                                verticle.service.getInstantiator().getI18NProvider() instanceof TestI18nProvider)));
    }

    public static class TestI18nProvider implements I18NProvider {

        @Override
        public List<Locale> getProvidedLocales() {
            return Collections.emptyList();
        }

        @Override
        public String getTranslation(String key, Locale locale, Object... params) {
            return "HELLO";
        }
    }

    private JsonObject testConfig() {
        return new JsonObject()
                .put("server", new JsonObject().put("port", 0))
                .put(
                        "vaadin",
                        new JsonObject()
                                .put("hilla.enabled", false)
                                .put("productionMode", true)
                                .put(
                                        "flowBasePackages",
                                        new JsonArray()
                                                .add(getClass().getPackage().getName())));
    }

    private static class TestVerticle extends VaadinVerticle {
        private AtomicBoolean startInvoked = new AtomicBoolean();
        private AtomicBoolean stopInvoked = new AtomicBoolean();
        VertxVaadinService service;

        @Override
        public void start() {
            startInvoked.set(true);
        }

        @Override
        public void stop() {
            stopInvoked.set(true);
        }

        @Override
        protected void serviceInitialized(VertxVaadinService service, Router router) {
            this.service = service;
            super.serviceInitialized(service, router);
        }
    }
}
