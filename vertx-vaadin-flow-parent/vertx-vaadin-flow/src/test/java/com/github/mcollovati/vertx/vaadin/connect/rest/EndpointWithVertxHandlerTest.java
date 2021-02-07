/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.mcollovati.vertx.vaadin.connect.rest;

import java.util.HashSet;
import java.util.function.Consumer;

import com.github.mcollovati.vertx.vaadin.connect.VaadinConnectEndpointRegistry;
import com.github.mcollovati.vertx.vaadin.connect.VaadinConnectHandler;
import com.github.mcollovati.vertx.vaadin.connect.VertxVaadinConnectEndpointService;
import com.github.mcollovati.vertx.vaadin.connect.auth.VaadinConnectAccessChecker;
import com.vaadin.flow.server.connect.EndpointNameChecker;
import com.vaadin.flow.server.connect.ExplicitNullableTypeChecker;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

/**
 * Source taken from Vaadin Flow (https://github.com/vaadin/flow)
 */
@RunWith(VertxUnitRunner.class)
public class EndpointWithVertxHandlerTest {

    private int port;
    private WebClient webClient;

    @Before
    public void setUp(TestContext context) throws Exception {
        Async async = context.async();
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        setupRouter(router);
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(0, ev -> {
                context.assertTrue(ev.succeeded(), "Http server started");
                HttpServer httpServer = ev.result();
                port = httpServer.actualPort();
                async.complete();
            });
        webClient = WebClient.create(vertx);
    }

    private void setupRouter(Router router) throws Exception {
        router.get("/api/get").handler(ctx -> {
            ctx.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encode(new BeanWithPrivateFields()));
        });

        HashSet<Class<?>> set = new HashSet<>();
        set.add(VaadinConnectEndpoints.class);
        /*
        MockServiceSessionSetup.TestVertxVaadinService service = new MockServiceSessionSetup().getService();
        when(service.getContext().getAttribute(VaadinConnectEndpointRegistry.class))
            .thenReturn(VaadinConnectEndpointRegistry.fromClasses(set));
        VaadinConnectHandler.register(router, service);
         */
        VertxVaadinConnectEndpointService service = new VertxVaadinConnectEndpointService(
            null, VaadinConnectEndpointRegistry.fromClasses(set),
            mock(EndpointNameChecker.class), mock(VaadinConnectAccessChecker.class),
            mock(ExplicitNullableTypeChecker.class)
        );
        VaadinConnectHandler.register(router, service);

    }

    @Test
    //https://github.com/vaadin/flow/issues/8010
    public void shouldNotExposePrivateAndProtectedFields_when_CallingFromRestAPIs(TestContext context) {
        Async async = context.async();
        webClient.get(port, "localhost", "/api/get")
            .putHeader("content-type", "application/json; charset=utf-8")
            .send(ev -> {
                context.assertTrue(ev.succeeded());
                context.assertEquals("{\"name\":\"Bond\"}", ev.result().bodyAsString());
                async.complete();
            });
    }

    @Test
    //https://github.com/vaadin/flow/issues/8034
    public void should_BeAbleToSerializePrivateFieldsOfABean_when_CallingFromConnectEndPoint(TestContext context) {
        callEndpointMethod("getBeanWithPrivateFields", context,
            result -> result.isEqualTo("{\"codeNumber\":\"007\",\"name\":\"Bond\",\"firstName\":\"James\"}"),
            "failed to serialize a bean with private fields"
        );
    }

    @Test
    //https://github.com/vaadin/flow/issues/8034
    public void should_BeAbleToSerializeABeanWithZonedDateTimeField(TestContext context) {
        callEndpointMethod("getBeanWithZonedDateTimeField", context,
            result -> result
                .isNotBlank()
                .doesNotContain("Failed to serialize endpoint 'VaadinConnectTypeConversionEndpoints' method 'getBeanWithZonedDateTimeField' response"),
            "failed to serialize a bean with ZonedDateTime field"
        );
    }

    @Test
    //https://github.com/vaadin/flow/issues/8067
    public void should_RepsectJacksonAnnotation_when_serializeBean(TestContext context) throws Exception {
        callEndpointMethod("getBeanWithJacksonAnnotation", context,
            result -> result.isEqualTo("{\"name\":null,\"rating\":2,\"bookId\":null}"),
            "failed to serialize a bean with Jackson annotated fields"
        );
    }

    @Test
    /**
     * this requires jackson-datatype-jsr310, which is added as a test scope dependency.
     * jackson-datatype-jsr310 is provided in spring-boot-starter-web, which is part of
     * vaadin-spring-boot-starter
     */
    public void should_serializeLocalTimeInExpectedFormat(TestContext context) {
        callEndpointMethod("getLocalTime", context,
            result -> result.isEqualTo("\"08:00:00\""),
            "failed to serialize a bean with jsr310 field"
        );
    }

    private void callEndpointMethod(String methodName, TestContext context,
                                    Consumer<AbstractStringAssert<?>> asserter,
                                    String failureMessage) {

        String endpointName = VaadinConnectEndpoints.class.getSimpleName();
        String requestUrl = String.format("/%s/%s", endpointName, methodName);
        Async async = context.async();
        webClient.post(port, "localhost", requestUrl)
            .putHeader("content-type", "application/json; charset=utf-8")
            .putHeader("accept", "application/json; charset=utf-8")
            .send(ev -> {
                context.assertTrue(ev.succeeded(), failureMessage);
                AbstractStringAssert<?> stringAssert = Assertions.assertThat(ev.result().bodyAsString());
                context.verify(v -> asserter.accept(stringAssert));
                async.complete();
            });
    }
}

