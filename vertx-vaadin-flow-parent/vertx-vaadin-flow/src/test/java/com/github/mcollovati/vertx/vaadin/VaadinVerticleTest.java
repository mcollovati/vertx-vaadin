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
import java.util.concurrent.atomic.AtomicBoolean;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VaadinVerticleTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void startWithPromiseShouldInvokeSynStart(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        DeploymentOptions opts = new DeploymentOptions()
            .setConfig(testConfig());
        rule.vertx().deployVerticle(verticle, opts, context.asyncAssertSuccess(ev ->
            context.assertTrue(verticle.startInvoked.get(), "Start not invoked")
        ));
    }

    @Test
    public void stopWithPromiseShouldInvokeSynStart(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        DeploymentOptions opts = new DeploymentOptions()
            .setConfig(testConfig());
        rule.vertx().deployVerticle(verticle, opts, context.asyncAssertSuccess(ev ->
            rule.vertx().undeploy(ev, context.asyncAssertSuccess(ev2 ->
                context.assertTrue(verticle.stopInvoked.get(), "Stop not invoked")
            ))
        ));
    }

    private JsonObject testConfig() {
        return new JsonObject()
            .put("server", new JsonObject().put("port", 0))
            .put("vaadin", new JsonObject()
                .put("productionMode", true)
                .put("flowBasePackages", new JsonArray(Collections.singletonList(getClass().getPackage().getName())))
            );
    }

    private static class TestVerticle extends VaadinVerticle {
        private AtomicBoolean startInvoked = new AtomicBoolean();
        private AtomicBoolean stopInvoked = new AtomicBoolean();

        @Override
        public void start() {
            startInvoked.set(true);
        }

        @Override
        public void stop() {
            stopInvoked.set(true);
        }
    }
}
