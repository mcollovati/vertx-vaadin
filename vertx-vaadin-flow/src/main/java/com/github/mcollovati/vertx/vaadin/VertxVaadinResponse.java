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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.server.VaadinResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by marco on 16/07/16.
 */
public class VertxVaadinResponse implements VaadinResponse {

    private static final Logger logger = LoggerFactory.getLogger(VertxVaadinResponse.class);

    private final RoutingContext routingContext;
    private final HttpServerResponse response;
    private final VertxVaadinService service;
    private Buffer outBuffer = Buffer.buffer();
    private boolean useOOS = false;
    private boolean useWriter = false;

    public VertxVaadinResponse(VertxVaadinService service, RoutingContext routingContext) {
        this.routingContext = routingContext;
        response = routingContext.response();
        this.service = service;
    }

    protected final RoutingContext getRoutingContext() {
        return routingContext;
    }

    @Override
    public void setStatus(int statusCode) {
        response.setStatusCode(statusCode);
    }

    @Override
    public void setContentType(String contentType) {
        response.putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    @Override
    public void setHeader(String name, String value) {
        response.putHeader(name, value);
    }

    @Override
    public void setDateHeader(String name, long timestamp) {
        response.putHeader(
                name,
                DateTimeFormatter.RFC_1123_DATE_TIME.format(
                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("GMT"))));
    }

    @Override
    public OutputStream getOutputStream() {
        if (useWriter) {
            throw new IllegalStateException("getWriter() has already been called for this response");
        }
        useOOS = true;
        return new OutputStream() {
            @Override
            public void write(int b) {
                outBuffer.appendByte((byte) b);
            }

            @Override
            public void close() {
                doClose();
            }

            @Override
            public void flush() {
                doFlush();
            }
        };
    }

    @Override
    public PrintWriter getWriter() {
        if (useOOS) {
            throw new IllegalStateException("getOutputStream() has already been called for this response");
        }
        useWriter = true;
        return new PrintWriter(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                outBuffer.appendString(new String(cbuf, off, len));
            }

            @Override
            public void flush() {
                doFlush();
            }

            @Override
            public void close() {
                doClose();
            }
        });
    }

    private void doFlush() {
        Buffer copy = outBuffer.copy();
        outBuffer = Buffer.buffer();
        if (!response.isChunked()) {
            response.setChunked(true);
        }
        response.write(copy);
    }

    private void doClose() {
        response.end(outBuffer);
    }

    @Override
    public void setCacheTime(long milliseconds) {
        doSetCacheTime(this, milliseconds);
    }

    @Override
    public void sendError(int errorCode, String message) throws IOException {
        response.setStatusCode(errorCode).end(message);
    }

    @Override
    public VertxVaadinService getService() {
        return service;
    }

    @Override
    public void addCookie(jakarta.servlet.http.Cookie cookie) {
        routingContext.addCookie(CookieUtils.toVertxCookie(cookie));
    }

    @Override
    public void setContentLength(int len) {
        response.putHeader(HttpHeaders.CONTENT_LENGTH, Integer.toString(len));
    }

    /**
     * Ends the response.
     * <p>
     * Does nothing if response is already endend or if it is chunked.
     */
    public void end() {
        // if (!response.ended() && !response.isChunked()) {
        if (!response.ended()) {
            response.end(outBuffer);
        }
    }

    private static void doSetCacheTime(VaadinResponse response, long milliseconds) {
        if (milliseconds <= 0) {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        } else {
            response.setHeader("Cache-Control", "max-age=" + milliseconds / 1000);
            response.setDateHeader("Expires", System.currentTimeMillis() + milliseconds);
            // Required to apply caching in some Tomcats
            response.setHeader("Pragma", "cache");
        }
    }
}
