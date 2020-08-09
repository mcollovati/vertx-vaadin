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
