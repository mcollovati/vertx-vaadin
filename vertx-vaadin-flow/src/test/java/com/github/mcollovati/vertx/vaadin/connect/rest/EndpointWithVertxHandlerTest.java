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
package com.github.mcollovati.vertx.vaadin.connect.rest;

import com.github.mcollovati.vertx.vaadin.connect.VaadinConnectHandler;
import com.github.mcollovati.vertx.vaadin.connect.VertxEndpointRegistry;
import com.github.mcollovati.vertx.vaadin.connect.VertxVaadinConnectEndpointService;
import com.github.mcollovati.vertx.vaadin.connect.auth.VaadinConnectAccessChecker;
import dev.hilla.EndpointNameChecker;
import dev.hilla.ExplicitNullableTypeChecker;
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

import java.util.HashSet;
import java.util.function.Consumer;

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

        VertxEndpointRegistry endpointRegistry = new VertxEndpointRegistry(mock(EndpointNameChecker.class));
        endpointRegistry.registerEndpoint(new VaadinConnectEndpoints());

        VertxVaadinConnectEndpointService service = new VertxVaadinConnectEndpointService(
                null, endpointRegistry, mock(VaadinConnectAccessChecker.class),
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

