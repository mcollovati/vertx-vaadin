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
package com.github.mcollovati.vertx.vaadin.connect;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.github.mcollovati.vertx.vaadin.connect.auth.VaadinConnectAccessChecker;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import dev.hilla.EndpointNameChecker;
import dev.hilla.ExplicitNullableTypeChecker;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.RoutingContext;

public class VertxVaadinConnectEndpointService
    extends BaseVaadinConnectEndpointService<RoutingContext, DefaultVaadinConnectResponse, VertxConnectRequestContext> {

    public VertxVaadinConnectEndpointService(
        ObjectMapper vaadinEndpointMapper, VaadinEndpointRegistry endpointRegistry,
        VaadinConnectAccessChecker<RoutingContext> accessChecker,
        ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        super(vaadinEndpointMapper, endpointRegistry, accessChecker, explicitNullableTypeChecker);
    }

    @Override
    protected ObjectMapper createVaadinConnectObjectMapper() {
        return DatabindCodec.mapper().copy()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    protected VaadinRequest createVaadinRequest(RoutingContext routingContext, VaadinService service) {
        return new VertxVaadinRequest((VertxVaadinService) service, routingContext);
    }
}