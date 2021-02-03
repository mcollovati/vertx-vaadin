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
 * Context to serve an endpoint request.
 *
 * @param <REQUEST> type of the request
 */
public interface EndpointServiceContext<REQUEST, RESPONSE> {

    int NOT_FOUND = 404;
    int UNAUTHORIZED = 401;
    int BAD_REQUEST = 400;
    int INTERNAL_SERVER_ERROR = 500;
    int OK = 200;

    REQUEST request();

    ObjectNode requestBody();

    RESPONSE respondWith(int statusCode, String body);

    default RESPONSE failWithNotFound() {
        return respondWith(NOT_FOUND, null);
    }

    default RESPONSE failWith(String body) {
        return respondWith(INTERNAL_SERVER_ERROR, body);
    }

    default RESPONSE failWithUnauthorized(String body) {
        return respondWith(UNAUTHORIZED, body);
    }

    default RESPONSE failWithBadRequest(String body) {
        return respondWith(BAD_REQUEST, body);
    }

    default RESPONSE respondWithOk(String body) {
        return respondWith(OK, body);
    }
}
