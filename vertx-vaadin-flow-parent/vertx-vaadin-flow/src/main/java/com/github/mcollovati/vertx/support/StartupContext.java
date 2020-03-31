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
package com.github.mcollovati.vertx.support;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.mcollovati.vertx.vaadin.VaadinOptions;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.impl.MimeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StartupContext {

    private static final Logger logger = LoggerFactory.getLogger(StartupContext.class);
    private final Set<String> resources;
    private final Context context;
    private final Vertx vertx;
    private final VaadinOptions vaadinOptions;

    private StartupContext(Vertx vertx, Set<String> resources, VaadinOptions vaadinOptions) {
        this.resources = new HashSet<>(resources);
        this.context = vertx.getOrCreateContext();
        this.vaadinOptions = vaadinOptions;
        this.vertx = vertx;
    }

    public Optional<String> resolveResource(String resource) {
        String normalized = resource.replaceFirst("^/", "");
        return resources.stream()
            .filter(r -> normalized.equals(r) || r.replaceFirst("^META-INF/resources/", "").equals(normalized))
            .findFirst();
    }

    public static Future<StartupContext> of(Vertx vertx, VaadinOptions vaadinOptions) {
        Promise<Set<String>> promise = Promise.promise();
        vertx.executeBlocking(StartupContext.scanResources(vaadinOptions), promise);
        return promise.future().map(res -> new StartupContext(vertx, res, vaadinOptions));
    }

    public static StartupContext syncOf(Vertx vertx, VaadinOptions vaadinOptions) {
        Promise<Set<String>> promise = Promise.promise();
        scanResources(vaadinOptions).handle(promise);
        return new StartupContext(vertx, promise.future().result(), vaadinOptions);
    }

    private static Handler<Promise<Set<String>>> scanResources(VaadinOptions vaadinOptions) {
        return future -> BootstrapHelper.scanResources(vaadinOptions, future::complete, future::fail);
    }

    public VaadinOptions vaadinOptions() {
        return vaadinOptions;
    }

    public ServletContext servletContext() {
        return new FakeServletContext();
    }

    private class FakeServletContext implements ServletContext {

        @Override
        public String getContextPath() {
            return vaadinOptions.mountPoint().replaceFirst("/$", "");
        }

        @Override
        public ServletContext getContext(String uripath) {
            return null;
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMajorVersion() {
            return 0;
        }

        @Override
        public int getEffectiveMinorVersion() {
            return 0;
        }

        @Override
        public String getMimeType(String file) {
            return MimeMapping.getMimeTypeForFilename(file);
        }

        @Override
        public Set<String> getResourcePaths(String path) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String relativePath = path;

            Pattern pattern;
            if (path.isEmpty()) {
                pattern = Pattern.compile("^((?:META-INF/resources/|)[^/]+(?:/|$))");
            } else {
                pattern = Pattern.compile(String.format("^((?:META-INF/resources/%1$s|%1$s)/[^/]+(?:/|$))", relativePath));
            }


            return resources.stream()
                .filter(p -> p.startsWith("META-INF/resources/" + relativePath) || p.startsWith(relativePath))
                .map(p -> {
                    Matcher matcher = pattern.matcher(p);
                    matcher.find();
                    return matcher.group(1);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public URL getResource(String path) throws MalformedURLException {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String path) {
            String relativePath = path;
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            FileSystem fileSystem = vertx.fileSystem();
            FileProps props = fileSystem.propsBlocking(relativePath);
            if (props != null && !props.isDirectory()) {
                Buffer buffer = fileSystem.readFileBlocking(relativePath);
                return new ByteArrayInputStream(buffer.getBytes());
            }
            return null;

        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public RequestDispatcher getNamedDispatcher(String name) {
            return null;
        }

        @Override
        public Servlet getServlet(String name) throws ServletException {
            return null;
        }

        @Override
        public Enumeration<Servlet> getServlets() {
            return null;
        }

        @Override
        public Enumeration<String> getServletNames() {
            return null;
        }

        @Override
        public void log(String msg) {
            logger.trace(msg);
        }

        @Override
        public void log(Exception exception, String msg) {
            logger.trace(msg, exception);
        }

        @Override
        public void log(String message, Throwable throwable) {
            logger.trace(message, throwable);
        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public String getServerInfo() {
            return null;
        }

        @Override
        public String getInitParameter(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;
        }

        @Override
        public boolean setInitParameter(String name, String value) {
            return false;
        }

        @Override
        public Object getAttribute(String name) {
            return context.get(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object object) {
            context.put(name, object);
        }

        @Override
        public void removeAttribute(String name) {
            context.remove(name);
        }

        @Override
        public String getServletContextName() {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, String className) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
            return null;
        }

        @Override
        public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
            return null;
        }

        @Override
        public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public ServletRegistration getServletRegistration(String servletName) {
            return null;
        }

        @Override
        public Map<String, ? extends ServletRegistration> getServletRegistrations() {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, String className) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
            return null;
        }

        @Override
        public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
            return null;
        }

        @Override
        public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public FilterRegistration getFilterRegistration(String filterName) {
            return null;
        }

        @Override
        public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
            return null;
        }

        @Override
        public SessionCookieConfig getSessionCookieConfig() {
            return null;
        }


        @Override
        public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            // Ignored on stub context
        }

        @Override
        public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
            return null;
        }

        @Override
        public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
            return null;
        }

        @Override
        public void addListener(String className) {
            // Ignored on stub context
        }

        @Override
        public <T extends EventListener> void addListener(T t) {
            // Ignored on stub context
        }

        @Override
        public void addListener(Class<? extends EventListener> listenerClass) {
            // Ignored on stub context
        }

        @Override
        public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
            return null;
        }

        @Override
        public JspConfigDescriptor getJspConfigDescriptor() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void declareRoles(String... roleNames) {
            // Ignored on stub context
        }

        @Override
        public String getVirtualServerName() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartupContext that = (StartupContext) o;
            return vertx.equals(that.vertx) &&
                vaadinOptions.equals(that.vaadinOptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vertx, vaadinOptions);
        }

    }
}