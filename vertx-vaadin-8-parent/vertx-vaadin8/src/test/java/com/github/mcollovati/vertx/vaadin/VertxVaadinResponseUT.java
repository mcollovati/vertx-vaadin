/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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

import javax.servlet.http.Cookie;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.mcollovati.vertx.utils.RandomStringGenerator;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by marco on 24/07/16.
 */
@RunWith(JUnitQuickcheck.class)
public class VertxVaadinResponseUT {

    private static final int TRIALS = 10;

    @Rule
    public MockitoRule mokitoRule = MockitoJUnit.rule();

    @Mock
    HttpServerResponse httpServerResponse;

    @Mock
    RoutingContext routingContext;

    @Mock
    VertxVaadinService vaadinService;

    VertxVaadinResponse vaadinResponse;

    @Before
    public void setUp() {
        when(routingContext.response()).thenReturn(httpServerResponse);
        vaadinResponse = new VertxVaadinResponse(vaadinService, routingContext);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateSetStatus(@InRange(minInt = 100, maxInt = 599) int status) throws Exception {
        vaadinResponse.setStatus(status);
        verify(httpServerResponse).setStatusCode(status);
    }

    @Test
    public void shouldDelegateSetContentType() throws Exception {
        String contentType = "text/html";
        vaadinResponse.setContentType(contentType);
        verify(httpServerResponse).putHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateSetHeader(@From(RandomStringGenerator.class) String name,
                                        @From(RandomStringGenerator.class) String value) throws Exception {
        vaadinResponse.setHeader(name, value);
        verify(httpServerResponse).putHeader(name, value);
    }

    @Test
    public void shouldDelegateSetDateHeader() throws Exception {
        assertDateHeader("HeaderName", LocalDateTime.of(1970, 1, 1, 0, 0, 0), "Thu, 1 Jan 1970 00:00:00");
        assertDateHeader("HeaderName", LocalDateTime.of(2016, 7, 24, 15, 53, 40), "Sun, 24 Jul 2016 15:53:40");
    }

    @Test
    public void shouldDelegateGetOutputStream() throws Exception {
        String test = "A test string";
        OutputStream oos = vaadinResponse.getOutputStream();
        assertThat(oos).isNotNull();
        oos.write(test.getBytes());
        oos.close();

        ArgumentCaptor<Buffer> byteBufferCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(httpServerResponse).end(byteBufferCaptor.capture());
        assertThat(byteBufferCaptor.getValue()).isEqualTo(Buffer.buffer(test.getBytes()));
    }

    @Test
    public void getOutputStreamShouldFailIfGetWriterHasBeenCalled() throws Exception {
        vaadinResponse.getWriter();
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(vaadinResponse::getOutputStream)
            .withMessage("getWriter() has already been called for this response");
    }

    @Test
    public void shouldDelegateGetWriter() throws Exception {
        String test = "A test string";
        PrintWriter writer = vaadinResponse.getWriter();
        writer.write(test);
        writer.flush();
        writer.write(test);
        writer.close();

        ArgumentCaptor<Buffer> byteBufferCaptor = ArgumentCaptor.forClass(Buffer.class);
        verify(httpServerResponse).write(byteBufferCaptor.capture());
        verify(httpServerResponse).end(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues())
            .containsExactly(Buffer.buffer(test.getBytes()), Buffer.buffer(test.getBytes()));
    }

    @Test
    public void getWriterShouldFailIfGetOutputStreamHasBeenCalled() throws Exception {
        vaadinResponse.getOutputStream();
        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(vaadinResponse::getWriter)
            .withMessage("getOutputStream() has already been called for this response");
    }


    @Test
    public void shouldDelegateSetCacheTimeForNoCache() throws Exception {
        vaadinResponse.setCacheTime(-1);
        verify(httpServerResponse).putHeader(HttpHeaders.CACHE_CONTROL.toString(), "no-cache");
        verify(httpServerResponse).putHeader("Pragma", "no-cache");
        assertDateHeader(HttpHeaders.EXPIRES.toString(), LocalDateTime.of(1970, 1, 1, 0, 0, 0), "Thu, 1 Jan 1970 00:00:00", false);
    }

    @Test
    public void shouldDelegateSetCacheTime() throws Exception {
        long millis = 3000;
        vaadinResponse.setCacheTime(millis);
        verify(httpServerResponse).putHeader(HttpHeaders.CACHE_CONTROL.toString(), "max-age=3");
        verify(httpServerResponse).putHeader(eq(HttpHeaders.EXPIRES.toString()), anyString());
        verify(httpServerResponse).putHeader("Pragma", "cache");
    }

    @Test
    public void shouldDelegateSendError() throws Exception {
        doReturn(httpServerResponse).when(httpServerResponse).setStatusCode(anyInt());
        vaadinResponse.sendError(500, "error");
        verify(httpServerResponse).setStatusCode(500);
        verify(httpServerResponse).end("error");
    }

    @Test
    public void shouldDelegateAadCookie() throws Exception {
        Set<io.vertx.ext.web.Cookie> cookies = new LinkedHashSet<>();
        Cookie cookie = new Cookie("name", "value");
        cookie.setMaxAge(10);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("path");
        cookie.setDomain("domain");
        vaadinResponse.addCookie(cookie);


        ArgumentCaptor<io.vertx.ext.web.Cookie> cookieCaptor = ArgumentCaptor.forClass(io.vertx.ext.web.Cookie.class);
        verify(routingContext).addCookie(cookieCaptor.capture());
        String expectedCookie = io.vertx.ext.web.Cookie.cookie(cookie.getName(), cookie.getValue())
            .setMaxAge(cookie.getMaxAge()).setSecure(cookie.getSecure())
            .setHttpOnly(cookie.isHttpOnly()).setPath(cookie.getPath())
            .setDomain(cookie.getDomain()).encode();
        assertThat(cookieCaptor.getValue().encode()).isEqualTo(expectedCookie);

    }

    @Test
    public void shouldDelegateSetContentLength() throws Exception {
        vaadinResponse.setContentLength(1000);
        verify(httpServerResponse).putHeader(HttpHeaders.CONTENT_LENGTH, "1000");
    }


    private void assertDateHeader(String headerName, LocalDateTime dateTime, String expected) {
        assertDateHeader(headerName, dateTime, expected, true);
    }
    private void assertDateHeader(String headerName, LocalDateTime dateTime, String expected, boolean invoke) {
        long epochMilli = dateTime.atZone(ZoneId.of("GMT")).toEpochSecond() * 1000;
        String offset = DateTimeFormatter.ofPattern(" zzz").format(
            Instant.ofEpochMilli(epochMilli).atZone(ZoneId.of("GMT"))
        );
        if (" +0000".equals(offset)) {
            offset = " GMT";
        }
        if (invoke) {
            vaadinResponse.setDateHeader(headerName, epochMilli);
        }
        verify(httpServerResponse).putHeader(headerName, expected + offset);
    }

}