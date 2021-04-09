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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.mcollovati.vertx.utils.RandomStringGenerator;
import com.github.mcollovati.vertx.web.ExtendedSession;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.auth.User;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.impl.ParsableLanguageValue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.list;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by marco on 16/07/16.
 */
@SuppressWarnings("unchecked")
@RunWith(JUnitQuickcheck.class)
public class VertxVaadinRequestUT {

    private static final int TRIALS = 10;
    @Rule
    public MockitoRule mokitoRule = MockitoJUnit.rule();

    @Mock
    HttpServerRequest httpServerRequest;
    @Mock
    RoutingContext routingContext;

    @Mock
    VertxVaadinService vaadinService;

    VertxVaadinRequest vaadinRequest;

    @Before
    public void setUp() {
        when(routingContext.request()).thenReturn(httpServerRequest);
        vaadinRequest = new VertxVaadinRequest(vaadinService, routingContext);
    }


    @Property(trials = TRIALS)
    public void shouldDelegateGetParameterToHttpServerRequest(@From(RandomStringGenerator.class) String paramName,
                                                              @From(RandomStringGenerator.class) String value) {
        when(httpServerRequest.getParam(paramName)).thenReturn(value);
        assertThat(vaadinRequest.getParameter(paramName)).isEqualTo(value);
        assertThat(vaadinRequest.getParameter(paramName + "NotExists")).isNull();
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetParameterMapToHttpServerRequest(Map<String, List<String>> map) {
        MultiMap multiMap = map.entrySet().stream().collect(MultiMap::caseInsensitiveMultiMap,
            (mm, kv) -> mm.add(kv.getKey(), kv.getValue()), MultiMap::addAll);
        when(httpServerRequest.params()).thenReturn(multiMap);
        Map.Entry<String, String[]>[] expected = map.entrySet().stream()
            .map(e -> entry(e.getKey(),
                e.getValue().stream().toArray(String[]::new)))
            .toArray(Map.Entry[]::new);

        assertThat(vaadinRequest.getParameterMap())
            .containsOnlyKeys(map.keySet().stream().toArray(String[]::new))
            .containsOnly(expected);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetContentLengthToHttpServerRequest(@InRange(minInt = 0, maxInt = 5000) int length) {
        when(routingContext.getBody()).thenReturn(Buffer.buffer(new byte[length]));
        assertThat(vaadinRequest.getContentLength()).isEqualTo(length);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetInputStreamToHttpServerRequest(String body) throws IOException {
        when(routingContext.getBody()).thenReturn(Buffer.buffer(body));
        assertThat(vaadinRequest.getInputStream())
            .hasSameContentAs(new ByteArrayInputStream(body.getBytes()));
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetReaderToHttpServerRequest(String body) throws IOException {
        when(routingContext.getBodyAsString()).thenReturn(body);
        assertThat(vaadinRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator())))
            .isEqualTo(body);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetAttributeToRoutingContext(@From(RandomStringGenerator.class) String paramName,
                                                           @From(RandomStringGenerator.class) String value) {
        when(routingContext.get(paramName)).thenReturn(value);
        assertThat(vaadinRequest.getAttribute(paramName)).isEqualTo(value);
        assertThat(vaadinRequest.getAttribute(paramName + "NotExists")).isNull();
    }

    @Property(trials = TRIALS)
    public void shouldDelegateSetAttributeToRoutingContext(@From(RandomStringGenerator.class) String paramName,
                                                           Object value) {
        vaadinRequest.setAttribute(paramName, value);
        verify(routingContext).put(paramName, value);
    }

    @Property(trials = TRIALS)
    public void shouldDelegateRemoveAttributeToRoutingContext(@From(RandomStringGenerator.class) String paramName) {
        vaadinRequest.removeAttribute(paramName);
        verify(routingContext).put(paramName, null);
    }

    @Test
    public void shouldDelegateGetAttributeNamesToRoutingContext() {
        Map<String, Object> data = new HashMap<String, Object>() {{
            put("a", "a");
            put("b", "b");
            put("c", "c");
        }};
        when(routingContext.data()).thenReturn(emptyMap(), data);
        assertThat(list(vaadinRequest.getAttributeNames())).isEmpty();
        assertThat(list(vaadinRequest.getAttributeNames())).contains("a", "b", "c");
    }

    @Test
    public void shouldDelegateGetPathInfo() {
        assertPathInfo("", "", "");
        assertPathInfo("", "/", "/");
        assertPathInfo("", "/path", "/path");
        assertPathInfo("", "/path/other", "/path/other");
        assertPathInfo("/ui", "/ui", "");
        assertPathInfo("/ui", "/ui/", "/");
        assertPathInfo("/ui", "/ui/path", "/path");
        assertPathInfo("/ui", "/ui/path/other", "/path/other");
    }

    @Test
    public void shouldDelegateGetContextPath() {
        when(routingContext.mountPoint()).thenReturn(null);
        assertThat(vaadinRequest.getContextPath()).isEqualTo("");
        when(routingContext.mountPoint()).thenReturn("");
        assertThat(vaadinRequest.getContextPath()).isEqualTo("");
        when(routingContext.mountPoint()).thenReturn("/ui");
        assertThat(vaadinRequest.getContextPath()).isEqualTo("/ui");
    }

    @Test
    public void shouldDelegateGetWrappedSessionToRoutingContext() {
        when(routingContext.session()).thenReturn(null);
        assertThat(vaadinRequest.getWrappedSession()).isNull();
        assertThat(vaadinRequest.getWrappedSession(true)).isNull();
        assertThat(vaadinRequest.getWrappedSession(false)).isNull();

        Session session = mock(ExtendedSession.class);
        when(routingContext.session()).thenReturn(session);
        assertThat(vaadinRequest.getWrappedSession()).isExactlyInstanceOf(VertxWrappedSession.class)
            .extracting(ws -> ((VertxWrappedSession) ws).getVertxSession()).isEqualTo(session);
        assertThat(vaadinRequest.getWrappedSession(true)).isExactlyInstanceOf(VertxWrappedSession.class)
            .extracting(ws -> ((VertxWrappedSession) ws).getVertxSession()).isEqualTo(session);
        assertThat(vaadinRequest.getWrappedSession(false)).isExactlyInstanceOf(VertxWrappedSession.class)
            .extracting(ws -> ((VertxWrappedSession) ws).getVertxSession()).isEqualTo(session);

    }

    @Test
    public void shouldDelegateGetContentType() {
        when(httpServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null, "text/html",
            "text/html;charset=UTF-8", "text/html; charset=UTF-8", "text/html; charset=UTF-8;",
            "text/plain; charset=ISO-8859-1; format=flowed");
        assertThat(vaadinRequest.getContentType()).isNull();
        assertThat(vaadinRequest.getContentType()).isEqualTo("text/html");
        assertThat(vaadinRequest.getContentType()).isEqualTo("text/html");
        assertThat(vaadinRequest.getContentType()).isEqualTo("text/html");
        assertThat(vaadinRequest.getContentType()).isEqualTo("text/html");
        assertThat(vaadinRequest.getContentType()).isEqualTo("text/plain");
    }

    @Test
    public void shouldDelegateGetLocale() {
        when(routingContext.preferredLanguage()).thenReturn(null)
            .thenReturn(new ParsableLanguageValue("en_US"));
        assertThat(vaadinRequest.getLocale()).isNull();
        assertThat(vaadinRequest.getLocale()).isEqualTo(java.util.Locale.forLanguageTag("en-US"));
    }

    @Test
    public void shouldDelegateGetRemoteAddress() {
        when(httpServerRequest.remoteAddress())
            .thenReturn(null)
            .thenReturn(new SocketAddressImpl(8080, "10.3.100.108"));
        assertThat(vaadinRequest.getRemoteAddr()).isNull();
        assertThat(vaadinRequest.getRemoteAddr()).isEqualTo("10.3.100.108");
    }

    @Test
    public void shouldDelegateIsSecure() {
        when(httpServerRequest.isSSL())
            .thenReturn(false).thenReturn(true);
        assertThat(vaadinRequest.isSecure()).isFalse();
        assertThat(vaadinRequest.isSecure()).isTrue();
    }

    @Property(trials = TRIALS)
    public void shouldDelegateGetHeader(@From(RandomStringGenerator.class) String name,
                                        @From(RandomStringGenerator.class) String value) {
        when(httpServerRequest.getHeader(name)).thenReturn(value);
        assertThat(vaadinRequest.getHeader(name)).isEqualTo(value);
        assertThat(vaadinRequest.getHeader(name + "notExist")).isNull();
    }

    @Test
    public void shouldDelegateGetHeaders() {
        when(httpServerRequest.headers()).thenReturn(
            MultiMap.caseInsensitiveMultiMap(),
            MultiMap.caseInsensitiveMultiMap().add("a", Arrays.<String>asList("1", "2"))
                .add("b", "b").add("c", "c")
        );
        assertThat(list(vaadinRequest.getHeaders("a"))).isEmpty();
        assertThat(list(vaadinRequest.getHeaders("a"))).containsExactly("1", "2");
        assertThat(list(vaadinRequest.getHeaders("b"))).containsExactly("b");
        assertThat(list(vaadinRequest.getHeaders("notPresent"))).isEmpty();
    }

    @Test
    public void shouldDelegateGetHeaderNames() {
        when(httpServerRequest.headers()).thenReturn(
            MultiMap.caseInsensitiveMultiMap(),
            MultiMap.caseInsensitiveMultiMap().add("a", "a").add("b", "b").add("c", "c")
        );
        assertThat(list(vaadinRequest.getHeaderNames())).isEmpty();
        assertThat(list(vaadinRequest.getHeaderNames())).contains("a", "b", "c");
    }

    @Test
    public void shouldDelegateGetCookies() {
        Cookie cookie1 = Cookie.cookie("cookie1", "value1")
            .setDomain("domain").setHttpOnly(true)
            .setMaxAge(90L).setPath("path").setSecure(true);
        Cookie cookie2 = Cookie.cookie("cookie2", "value2");
        when(routingContext.cookieCount()).thenReturn(0).thenReturn(2);
        when(routingContext.cookieMap()).thenReturn(Stream.of(cookie1, cookie2).collect(Collectors.toMap(Cookie::getName, identity())));
        assertThat(vaadinRequest.getCookies()).isNull();
        javax.servlet.http.Cookie[] cookies = vaadinRequest.getCookies();
        assertThat(cookies).hasSize(2);
        assertThat(cookies[0]).extracting("name", "value", "domain", "httpOnly", "maxAge", "path", "secure")
            .containsExactly(cookie1.getName(), cookie1.getValue(), cookie1.getDomain(), true, 90, "path", true);
        assertThat(cookies[1]).extracting("name", "value", "domain", "httpOnly", "maxAge", "path", "secure")
            .containsExactly(cookie2.getName(), cookie2.getValue(), null, false, -1, null, false);
    }

    @Test
    @Ignore
    public void shouldDelegateGetAuthType() {

    }

    @Test
    public void shouldDelegateGetRemoteUser() {
        User user = mock(User.class);
        when(user.principal())
            .thenReturn(new JsonObject().put("username", "marco"))
            .thenReturn(new JsonObject());
        when(routingContext.user()).thenReturn(null).thenReturn(user);
        assertThat(vaadinRequest.getRemoteUser()).isNull();
        assertThat(vaadinRequest.getRemoteUser()).isEqualTo("marco");
        assertThat(vaadinRequest.getRemoteUser()).isNull();
    }

    @Test
    public void shouldDelegateGetPrincipal() {
        User user = mock(User.class);
        when(user.principal())
            .thenReturn(new JsonObject().put("username", "marco"))
            .thenReturn(new JsonObject());
        when(routingContext.user()).thenReturn(null).thenReturn(user);
        assertThat(vaadinRequest.getUserPrincipal()).isNull();
        assertThat(vaadinRequest.getUserPrincipal().getName()).isEqualTo("marco");
        assertThat(vaadinRequest.getUserPrincipal().getName()).isNull();
    }

    @Test
    public void shouldDelegateIsUserInRole() {
        User user = mock(User.class);
        doAnswer(invocation -> {
            String role = invocation.getArgumentAt(0, String.class);
            Handler<AsyncResult<Boolean>> handler = invocation.getArgumentAt(1, Handler.class);
            handler.handle(Future.succeededFuture("USER".equals(role)));
            return user;
        }).when(user).isAuthorized(isA(String.class), isA(Handler.class));
        when(user.principal())
            .thenReturn(new JsonObject().put("username", "marco"))
            .thenReturn(new JsonObject());
        when(routingContext.user()).thenReturn(null).thenReturn(user);
        assertThat(vaadinRequest.isUserInRole("USER")).isFalse();
        assertThat(vaadinRequest.isUserInRole("ADMIN")).isFalse();
        assertThat(vaadinRequest.isUserInRole("USER")).isTrue();
        //assertThat(vaadinRequest.getUserPrincipal().getName()).isNull();
    }

    @Test
    public void shouldDelegateGetLocalesToRoutingContext() {
        when(routingContext.acceptableLanguages()).thenReturn(emptyList(), Arrays.asList(
            new ParsableLanguageValue("it"), new ParsableLanguageValue("en_US"),
            new ParsableLanguageValue("de")
        ));
        assertThat(list(vaadinRequest.getLocales())).isEmpty();
        assertThat(list(vaadinRequest.getLocales())).contains(
            new java.util.Locale("it"), new java.util.Locale("en", "US"), new java.util.Locale("de")
        );
    }

    @Test
    public void shouldDelegateGetRemoteHost() {
        when(httpServerRequest.remoteAddress()).thenReturn(null, new SocketAddressImpl(5555, "10.1.1.1"));
        assertThat(vaadinRequest.getRemoteAddr()).isNull();
        assertThat(vaadinRequest.getRemoteAddr()).isEqualTo("10.1.1.1");
    }

    @Test
    public void shouldDelegateGetRemotePort() {
        when(httpServerRequest.remoteAddress()).thenReturn(null, new SocketAddressImpl(5555, "10.1.1.1"));
        assertThat(vaadinRequest.getRemotePort()).isEqualTo(-1);
        assertThat(vaadinRequest.getRemotePort()).isEqualTo(5555);
    }

    @Test
    public void shouldDelegateGetCharacterEncoding() {
        when(httpServerRequest.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null, "text/html",
            "text/html;charset=UTF-8", "text/html; charset=UTF-8", "text/html; charset=UTF-8;",
            "text/plain; charset=ISO-8859-1; format=flowed");
        assertThat(vaadinRequest.getCharacterEncoding()).isNull();
        assertThat(vaadinRequest.getCharacterEncoding()).isNull();
        assertThat(vaadinRequest.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(vaadinRequest.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(vaadinRequest.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(vaadinRequest.getCharacterEncoding()).isEqualTo("ISO-8859-1");
    }

    @Test
    public void shouldDelegateGetMethod() {
        HttpMethod.values().forEach(met -> {
            when(httpServerRequest.method()).thenReturn(met);
            assertThat(vaadinRequest.getMethod()).isEqualTo(met.name());
        });
        verify(httpServerRequest,times(HttpMethod.values().size())).method();
    }

    @Test
    public void shouldDelegateGetDateHeader() {
        ZonedDateTime headerValue = LocalDateTime.of(2016, 7, 3, 8, 49, 37)
            .atZone(ZoneId.of("GMT"));
        long longHeaderValue = headerValue.toEpochSecond() * 1000;
        when(httpServerRequest.getHeader(HttpHeaders.IF_MODIFIED_SINCE.toString()))
            .thenReturn(null)
            .thenReturn("Sun, 03 Jul 2016 08:49:37 GMT")
            .thenReturn("Sunday, 03-Jul-16 08:49:37 GMT")
            .thenReturn("Sun Jul  3 08:49:37 2016");
        assertThat(vaadinRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE.toString())).isEqualTo(-1);
        assertThat(vaadinRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE.toString())).isEqualTo(longHeaderValue);
        assertThat(vaadinRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE.toString())).isEqualTo(longHeaderValue);
        assertThat(vaadinRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE.toString())).isEqualTo(longHeaderValue);
    }


    private void assertPathInfo(String mountPoint, String path, String expected) {
        when(httpServerRequest.path()).thenReturn(path);
        when(routingContext.mountPoint()).thenReturn(mountPoint);
        assertThat(vaadinRequest.getPathInfo()).isEqualTo(expected);
    }

}
