package com.github.mcollovati.vertx.vaadin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.mcollovati.failures.lookup.FakeLookupInitializer;
import com.github.mcollovati.vertx.support.StartupContext;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinConfigurationException;

@RunWith(VertxUnitRunner.class)
public class VaadinVerticleUT {

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

    @Test
    public void shouldPropagateFailureIfInitializationFails(TestContext context) {
        TestVerticle verticle = new TestVerticle();
        JsonObject config = testConfig();
        config.getJsonObject("vaadin").getJsonArray("flowBasePackages")
                .add(FakeLookupInitializer.class.getPackage().getName());
        DeploymentOptions opts = new DeploymentOptions().setConfig(config);
        rule.vertx().deployVerticle(verticle, opts, context.asyncAssertFailure(ev ->
                Assertions.assertThat(ev).isExactlyInstanceOf(VaadinConfigurationException.class)
        ));
    }

    private JsonObject testConfig() {
        return new JsonObject()
                .put("server", new JsonObject().put("port", 0))
                .put("vaadin", new JsonObject()
                        .put("compatibilityMode", true)
                        .put("productionMode", true)
                        .put("flowBasePackages", new JsonArray(new ArrayList<>(
                                Collections.singletonList(getClass().getPackage().getName()))
                        ))
                );
    }

    private static class TestVerticle extends VaadinVerticle {
        private final AtomicBoolean startInvoked = new AtomicBoolean();
        private final AtomicBoolean stopInvoked = new AtomicBoolean();

        @Override
        public void start() {
            startInvoked.set(true);
        }

        @Override
        public void stop() {
            stopInvoked.set(true);
        }

        @Override
        protected VertxVaadin createVertxVaadin(StartupContext startupContext) {
            return new VertxVaadin(Vertx.vertx(), startupContext) {
                @Override
                protected VertxVaadinService createVaadinService() {
                    VertxVaadinService vaadinService = Mockito.spy(super.createVaadinService());
                    try {
                        Mockito.doNothing().when(vaadinService).init();
                        Mockito.doNothing().when(vaadinService).destroy();
                    } catch (ServiceException e) {
                        // ignore
                    }
                    return vaadinService;
                }
            };
        }
    }
}
