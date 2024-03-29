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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A support class to create technology specific {@link EndpointServiceContext}s.
 * <p>
 * Base class only provides accessors for {@code REQUEST} and request body.
 *
 * @param <REQUEST>  type of the request.
 * @param <RESPONSE> type of the response.
 */
public abstract class EndpointServiceContextSupport<REQUEST, RESPONSE>
        implements EndpointServiceContext<REQUEST, RESPONSE> {
    private final REQUEST request;
    private final ObjectNode body;

    public EndpointServiceContextSupport(REQUEST request, ObjectNode body) {
        this.request = request;
        this.body = body;
    }

    public REQUEST request() {
        return request;
    }

    public ObjectNode requestBody() {
        return body;
    }
}
