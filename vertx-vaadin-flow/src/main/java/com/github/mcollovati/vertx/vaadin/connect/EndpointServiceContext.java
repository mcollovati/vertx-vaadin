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

    /**
     * {@code 200 OK}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.3.1">HTTP/1.1: Semantics and Content, section 6.3.1</a>
     */
    int OK = 200;

    /**
     * {@code 400 Bad Request}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.1">HTTP/1.1: Semantics and Content, section 6.5.1</a>
     */
    int BAD_REQUEST = 400;
    /**
     * {@code 401 Unauthorized}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1: Authentication, section 3.1</a>
     */
    int UNAUTHORIZED = 401;
    /**
     * {@code 403 Forbidden}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.3">HTTP/1.1: Semantics and Content, section 6.5.3</a>
     */
    int FORBIDDEN = 403;
    /**
     * {@code 404 Not Found}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
     */
    int NOT_FOUND = 404;
    /**
     * {@code 500 Internal Server Error}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-6.6.1">HTTP/1.1: Semantics and Content, section 6.6.1</a>
     */
    int INTERNAL_SERVER_ERROR = 500;

    /**
     * Returns the current request.
     *
     * @return current request.
     */
    REQUEST request();

    /**
     * Returns the request body as a JSON object.
     *
     * @return the request body, or {@literal null} if request has no body.
     */
    ObjectNode requestBody();

    /**
     * Creates a {@code RESPONSE} instance for the given {@code statusCode} and {@code body}.
     *
     * @param statusCode response status code.
     * @param body       response body, may be {@literal null}.
     * @return an instance of {@code RESPONSE}, never {@literal null}.
     */
    RESPONSE respondWith(int statusCode, String body);

    /**
     * Creates a failure {@code RESPONSE} with status code 400 and {@literal null} body.
     *
     * @return a {@code RESPONSE} with status code 400 and {@literal null} body.
     * @see #NOT_FOUND
     */
    default RESPONSE failWithNotFound() {
        return respondWith(NOT_FOUND, null);
    }

    /**
     * Creates a failure {@code RESPONSE} with status code 500 and given body.
     *
     * @return a {@code RESPONSE} with status code 400 and given body.
     * @see #INTERNAL_SERVER_ERROR
     */
    default RESPONSE failWith(String body) {
        return respondWith(INTERNAL_SERVER_ERROR, body);
    }

    /**
     * Creates a failure {@code RESPONSE} with status code 401 and given body.
     *
     * @return a {@code RESPONSE} with status code 401 and given body.
     * @see #UNAUTHORIZED
     */
    default RESPONSE failWithUnauthorized(String body) {
        return respondWith(UNAUTHORIZED, body);
    }

    /**
     * Creates a failure {@code RESPONSE} with status code 403 and given body.
     *
     * @return a {@code RESPONSE} with status code 403 and given body.
     * @see #FORBIDDEN
     */
    default RESPONSE failWithForbidden(String body) {
        return respondWith(FORBIDDEN, body);
    }

    /**
     * Creates a failure {@code RESPONSE} with status code 400 and given body.
     *
     * @return a {@code RESPONSE} with status code 400 and given body.
     * @see #BAD_REQUEST
     */
    default RESPONSE failWithBadRequest(String body) {
        return respondWith(BAD_REQUEST, body);
    }

    /**
     * Creates a successful {@code RESPONSE} with status code 200 and given body.
     *
     * @return a {@code RESPONSE} with status code 200 and given body.
     * @see #OK
     */
    default RESPONSE respondWithOk(String body) {
        return respondWith(OK, body);
    }
}
