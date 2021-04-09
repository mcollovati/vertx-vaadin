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

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.mcollovati.vertx.Sync;
import com.github.mcollovati.vertx.web.ExtendedSession;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedSession;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.LanguageHeader;
import io.vertx.ext.web.RoutingContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by marco on 16/07/16.
 */
public class VertxVaadinRequest implements VaadinRequest {

    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile("^(.*/[^;]+)(?:;.*$|$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CHARSET_PATTERN = Pattern.compile("^.*(?<=charset=)([^;]+)(?:;.*$|$)", Pattern.CASE_INSENSITIVE);

    private final VertxVaadinService service;
    private final RoutingContext routingContext;
    private final HttpServerRequest request;

    public VertxVaadinRequest(VertxVaadinService service, RoutingContext routingContext) {
        this.service = service;
        this.routingContext = routingContext;
        this.request = routingContext.request();

    }

    public HttpServerRequest getRequest() {
        return request;
    }

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    @Override
    public String getParameter(String parameter) {
        return request.getParam(parameter);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.params().names()
            .stream().collect(toMap(identity(), k -> request.params().getAll(k).toArray(new String[0])));
    }

    @Override
    public int getContentLength() {
        return routingContext.getBody().length();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(routingContext.getBody().getBytes());
    }

    @Override
    public Object getAttribute(String name) {
        return routingContext.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        routingContext.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        routingContext.put(name, null);
    }

    @Override
    public String getPathInfo() {
        return request.path().substring(getContextPath().length());
    }

    @Override
    public String getContextPath() {
        return Optional.ofNullable(routingContext.mountPoint()).orElse("");
    }

    @Override
    public WrappedSession getWrappedSession() {
        return getWrappedSession(true);
    }

    @Override
    public WrappedSession getWrappedSession(boolean allowSessionCreation) {
        return Optional.ofNullable(routingContext.session())
            .map(ExtendedSession::adapt)
            .map(VertxWrappedSession::new).orElse(null);
    }

    @Override
    public String getContentType() {
        return Optional.ofNullable(request.getHeader(HttpHeaders.CONTENT_TYPE))
            .map(CONTENT_TYPE_PATTERN::matcher).filter(Matcher::matches)
            .map(m -> m.group(1)).orElse(null);
    }

    @Override
    public Locale getLocale() {
        return toJavaLocale(routingContext.preferredLanguage());
    }

    @Override
    public String getRemoteAddr() {
        return Optional.ofNullable(request.remoteAddress())
            .map(SocketAddress::host).orElse(null);
    }

    @Override
    public boolean isSecure() {
        return request.isSSL();
    }

    @Override
    public String getHeader(String headerName) {
        return request.getHeader(headerName);
    }

    @Override
    public VertxVaadinService getService() {
        return service;
    }

    @Override
    public Cookie[] getCookies() {
        if (routingContext.cookieCount() > 0) {
            return routingContext.cookieMap().values().stream().map(CookieUtils::fromVertxCookie).toArray(Cookie[]::new);
        }
        return null;
    }

    // TODO
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return Optional.ofNullable(routingContext.user())
            .map(User::principal).flatMap(json -> Optional.ofNullable(json.getString("username")))
            .orElse(null);
    }

    @Override
    public Principal getUserPrincipal() {
        return Optional.ofNullable(routingContext.user())
            .map(VertxPrincipal::new)
            .orElse(null);
    }

    // TODO
    @Override
    public boolean isUserInRole(String role) {
        if (routingContext.user() != null) {
            return Sync.await(completer -> routingContext.user().isAuthorized(role, completer));
        }
        return false;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(routingContext.data().keySet());
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(routingContext.acceptableLanguages().stream()
            .map(VertxVaadinRequest::toJavaLocale).collect(toList()));
    }

    @Override
    public String getRemoteHost() {
        return request.host();
    }

    @Override
    public int getRemotePort() {
        return Optional.ofNullable(request.remoteAddress())
            .map(SocketAddress::port).orElse(-1);
    }

    @Override
    public String getCharacterEncoding() {
        return Optional.ofNullable(request.getHeader(HttpHeaders.CONTENT_TYPE))
            .map(CHARSET_PATTERN::matcher).filter(Matcher::matches)
            .map(m -> m.group(1)).orElse(null);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new StringReader(routingContext.getBodyAsString()));
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public long getDateHeader(String name) {
        return Optional.ofNullable(request.getHeader(name))
            .flatMap(s -> tryParseDate(s, DateTimeFormatter.RFC_1123_DATE_TIME,
                DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz"),
                DateTimeFormatter.ofPattern("EEE MMM ppd HH:mm:ss yyyy").withZone(ZoneId.of("GMT"))
            )).orElse(-1L);
    }

    private Optional<Long> tryParseDate(String date, DateTimeFormatter... formatter) {
        return Stream.of(formatter)
            .findFirst().map(f -> {
                try {
                    return Optional.of(ZonedDateTime.parse(date, f).toEpochSecond() * 1000);
                } catch (DateTimeParseException ex) {
                    return tryParseDate(date, Stream.of(formatter).skip(1).toArray(DateTimeFormatter[]::new));
                }
            }).orElse(Optional.empty());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.headers().names());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(request.headers().getAll(name));
    }

    private static Locale toJavaLocale(LanguageHeader locale) {
        return Optional.ofNullable(locale)
            .map(loc -> new Locale(loc.tag(),
                Optional.ofNullable(loc.subtag()).orElse(""),
                Optional.ofNullable(loc.subtag(2)).orElse("")
            ))
            .orElse(null);
    }

    public static Optional<VertxVaadinRequest> tryCast(VaadinRequest request) {
        if (request instanceof VertxVaadinRequest) {
            return Optional.of((VertxVaadinRequest) request);
        }
        return Optional.empty();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static class VertxPrincipal implements Principal {

        private final User user;

        @Override
        public String getName() {
            return user.principal().getString("username");
        }
    }

}
