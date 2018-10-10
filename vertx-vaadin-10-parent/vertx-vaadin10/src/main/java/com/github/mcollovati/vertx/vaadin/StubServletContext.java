package com.github.mcollovati.vertx.vaadin;

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
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.core.file.FileSystem;

// Stub class only used to get access to com.vaadin.flow.server.startup.RouteRegistry
// through vertx Context
class StubServletContext implements ServletContext {

    //private final LocalMap<String, Object> context;
    private final Context context;
    private final Vertx vertx;
    private final Map<String, Set<String>> resourcesCaches;


    public StubServletContext(Vertx vertx) {
        this.vertx = vertx;
        //this.context = vertx.sharedData().getLocalMap(ServletContext.class.getName());
        this.context = vertx.getOrCreateContext();
        this.resourcesCaches = new HashMap<>();
        /*
        new FastClasspathScanner()
            .alwaysScanClasspathElementRoot()
            .matchFilenamePattern("META-INF/resources/.*", (File classpathElt, String relativePath) -> {
                System.out.println("===================== " + classpathElt + " ---> " + relativePath);
                relativePath.split("/");
                if (classpathElt.isDirectory()) {
                    resourcesCaches.put(relativePath, new LinkedHashSet<>());
                }
            })
            //.verbose()
            .removeTemporaryFilesAfterScan(true)
            .scan();
        */
    }

    @Override
    public String getContextPath() {
        return null;
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
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        String relativePath = path;
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        //FileSystem fileSystem = vertx.fileSystem();

        return Stream.of(relativePath, "META-INF/resources/" + relativePath).distinct()
            .flatMap(this::resolveRelativeResourcePath)
            //.map(p -> p.replaceFirst("META-INF/resources/", ""))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        /*
        FileProps props = fileSystem.propsBlocking(relativePath);
        if (props != null && props.isDirectory()) {
            System.out.println("============= " + path + " Is directory");
            return Stream.of(relativePath, "META-INF/resources/" + relativePath).distinct()
                .filter(fileSystem::existsBlocking)
                .flatMap(p -> fileSystem.readDirBlocking(p).stream())
                .map(p -> {
                        String n = Paths.get(p).getFileName().toString();
                        if (fileSystem.propsBlocking(p).isDirectory()) {
                            n += "/";
                        }
                        return path + n;
                    }
                ).collect(Collectors.toCollection(LinkedHashSet::new));

            /*
            return fileSystem.readDirBlocking(relativePath).stream()
                .map(p -> {
                    String n = Paths.get(p).getFileName().toString();
                    if (fileSystem.propsBlocking(p).isDirectory()) {
                        n += "/";
                    }
                    return path + n;
                }).collect(Collectors.toCollection(LinkedHashSet::new));* /
        }
        System.out.println("============= " + path + " Is not directory. " + props);
        return null;
        */
    }


    private Stream<String> resolveRelativeResourcePath(String path) {
        FileSystem fileSystem = vertx.fileSystem();
        if (fileSystem.existsBlocking(path)) {
            FileProps props = fileSystem.propsBlocking(path);
            if (props != null && props.isDirectory()) {
                System.out.println("============= " + path + " Is directory");
                return fileSystem.readDirBlocking(path).stream()
                    .map(p -> {
                        String n = Paths.get(p).getFileName().toString();
                        if (fileSystem.propsBlocking(p).isDirectory()) {
                            n += "/";
                        }
                        System.out.println("==================== " + path + " ---> " + p);
                        return path + n;
                    });
            }
        }
        return Stream.empty();
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

    }

    @Override
    public void log(Exception exception, String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

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

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

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

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }
}

