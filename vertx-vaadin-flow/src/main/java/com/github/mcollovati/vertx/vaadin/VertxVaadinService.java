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

import jakarta.servlet.ServletContext;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.server.Mode;
import com.vaadin.flow.server.PwaRegistry;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.ServiceContextUriResolver;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.FaviconHandler;
import com.vaadin.flow.server.communication.IndexHtmlRequestHandler;
import com.vaadin.flow.server.communication.WebComponentProvider;
import com.vaadin.flow.server.startup.ApplicationRouteRegistry;
import com.vaadin.flow.shared.ApplicationConstants;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.impl.FileResolverImpl;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.file.FileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.support.BufferInputStreamAdapter;
import com.github.mcollovati.vertx.vaadin.communication.RequestHandlerReplacements;
import com.github.mcollovati.vertx.vaadin.communication.VertxIndexHtmlRequestHandler;

/**
 * Created by marco on 16/07/16.
 */
public class VertxVaadinService extends VaadinService {

    private static final Logger logger = LoggerFactory.getLogger(VertxVaadinService.class);

    private final transient VertxVaadin vertxVaadin;
    private final ServiceContextUriResolver contextResolver = new ServiceContextUriResolver();

    public VertxVaadinService(VertxVaadin vertxVaadin, DeploymentConfiguration deploymentConfiguration) {
        super(deploymentConfiguration);
        this.vertxVaadin = vertxVaadin;
    }

    protected final VertxVaadin getVertxVaadin() {
        return vertxVaadin;
    }

    public Vertx getVertx() {
        return vertxVaadin.vertx();
    }

    public VaadinOptions getVaadinOptions() {
        return vertxVaadin.config();
    }

    public ServletContext getServletContext() {
        return vertxVaadin.servletContext();
    }

    @Override
    public void ensureAccessQueuePurged(VaadinSession session) {
        Context context = getVertx().getOrCreateContext();
        // Ensure commands are executed in vertx context
        context.runOnContext(ev -> super.ensureAccessQueuePurged(session));
    }

    @Override
    protected RouteRegistry getRouteRegistry() {
        return ApplicationRouteRegistry.getInstance(getContext());
    }

    @Override
    protected PwaRegistry getPwaRegistry() {
        return PwaRegistry.getInstance(vertxVaadin.servletContext());
    }

    /*
    @Override
    protected Instantiator createInstantiator() throws ServiceException {
        return new VertxVaadinInstantiator(super.createInstantiator());
    }
     */

    @Override
    protected List<RequestHandler> createRequestHandlers() throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        // TODO: removed because of explicit cast to servlet; should be handled at router level?
        handlers.removeIf(FaviconHandler.class::isInstance);
        handlers.replaceAll(RequestHandlerReplacements::replace);

        Mode mode = getDeploymentConfiguration().getMode();
        if (mode == Mode.DEVELOPMENT_FRONTEND_LIVERELOAD || mode == Mode.DEVELOPMENT_BUNDLE) {
            Optional<DevModeHandler> handlerManager = DevModeHandlerManager.getDevModeHandler(this);
            if (handlerManager.isPresent()) {
                DevModeHandler devModeHandler = handlerManager.get();
                // WebComponentProvider handler should run before DevModeHandler
                // to avoid responding with html contents when dev bundle is
                // not ready (e.g. dev-mode-not-ready.html)
                handlers.stream()
                        .filter(WebComponentProvider.class::isInstance)
                        .findFirst()
                        .map(handlers::indexOf)
                        .ifPresentOrElse(
                                idx -> {
                                    handlers.add(idx, devModeHandler);
                                },
                                () -> handlers.add(devModeHandler));
            } else if (mode == Mode.DEVELOPMENT_FRONTEND_LIVERELOAD) {
                logger.warn("no DevModeHandlerManager implementation found "
                        + "but dev server enabled. Include the "
                        + "com.vaadin.vaadin-dev-server dependency.");
            }
        }
        addBootstrapHandler(handlers);
        return handlers;
    }

    private void addBootstrapHandler(List<RequestHandler> handlers) {
        handlers.add(0, new VertxIndexHtmlRequestHandler());
        logger.debug("Using '{}' in client mode bootstrapping", VertxIndexHtmlRequestHandler.class.getName());
    }

    @Override
    public String getMimeType(String resourceName) {
        return MimeMapping.getMimeTypeForFilename(resourceName);
    }

    @Override
    public boolean ensurePushAvailable() {
        return vertxVaadin.config().supportsSockJS();
    }

    @Override
    public String getMainDivId(VaadinSession session, VaadinRequest request) {
        String appId;
        // Have to check due to VertxBootstrapHandler tricks
        if (request instanceof VertxVaadinRequest) {
            appId = ((VertxVaadinRequest) request).getRoutingContext().mountPoint();
        } else {
            appId = request.getContextPath();
        }

        if (appId == null || "".equals(appId) || "/".equals(appId)) {
            appId = "ROOT";
        }
        appId = appId.replaceAll("[^a-zA-Z0-9]", "");
        // Add hashCode to the end, so that it is still (sort of)
        // predictable, but indicates that it should not be used in CSS
        // and
        // such:
        int hashCode = appId.hashCode();
        if (hashCode < 0) {
            hashCode = -hashCode;
        }
        appId = appId + "-" + hashCode;
        return appId;
    }

    // From VaadinServletService
    @Override
    protected boolean requestCanCreateSession(VaadinRequest request) {
        if (isOtherRequest(request)) {
            /*
             * I.e URIs that are not RPC calls or static (theme) files.
             */
            return true;
        }

        return false;
    }

    // From VaadinServletService
    private boolean isOtherRequest(VaadinRequest request) {
        String type = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return type == null || ApplicationConstants.REQUEST_TYPE_INIT.equals(type);
    }

    @Override
    public String getServiceName() {
        return vertxVaadin.serviceName();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public URL getStaticResource(String url) {
        return tryResolveFile(url);
    }

    @Override
    public URL getResource(String url) {
        return tryGetResource(resolveResource(url));
    }

    @Override
    public InputStream getResourceAsStream(String url) {
        return tryGetResourceAsStream(resolveResource(url));
    }

    @Override
    public String resolveResource(String url) {
        Objects.requireNonNull(url, "Url cannot be null");

        return contextResolver.resolveVaadinUri(url);
    }

    @Override
    protected VertxVaadinContext constructVaadinContext() {
        return vertxVaadin.newVaadinContext();
    }

    private URL tryGetResource(String path) {
        logger.trace("Try to resolve path {}", path);
        URL url = tryResolveFile(path);
        if (url == null && !path.startsWith("META-INF/resources/")) {
            logger.trace("Path {} not found, try into /META-INF/resources/", path);
            url = tryResolveFile("/META-INF/resources/" + path);
        }
        /*
        if (url == null) {
            logger.trace("Path {} not found into META-INF/resources/, try with webjars", path);
            url = vertxVaadin.webJars.getWebJarResourcePath(path)
                .map(this::tryResolveFile).orElse(null);
        }*/
        return url;
    }

    private URL tryResolveFile(String path) {
        Vertx vertx = getVertx();
        FileSystem fileSystem = vertx.fileSystem();
        String relativePath = makePathRelative(path);
        if (fileSystem.existsBlocking(relativePath)) {
            try {
                FileResolver fileResolver = (vertx instanceof VertxInternal)
                        ? ((VertxInternal) vertx).fileResolver()
                        : new FileResolverImpl();
                return fileResolver.resolveFile(relativePath).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public InputStream tryGetResourceAsStream(String path) {
        logger.trace("Try to resolve path {}", path);
        String relativePath = makePathRelative(path);
        FileSystem fileSystem = getVertx().fileSystem();
        if (fileSystem.existsBlocking(relativePath)) {
            return new BufferInputStreamAdapter(fileSystem.readFileBlocking(relativePath));
        }
        if (!path.startsWith("/META-INF/resources")) {
            logger.trace("Path {} not found, try into /META-INF/resources/", path);
            InputStream is = tryGetResourceAsStream("/META-INF/resources/" + relativePath);
            if (is != null) {
                return is;
            }
        }
        /*
        logger.trace("Path {} not found into META-INF/resources/, try with webjars");
        return vertxVaadin.webJars.getWebJarResourcePath(path)
            .filter(fileSystem::existsBlocking)
            .map(fileSystem::readFileBlocking)
            .map(BufferInputStreamAdapter::new)
            .orElse(null);
         */
        return null;
    }

    private String makePathRelative(String path) {
        String relativePath = path;
        if (path.startsWith("/")) {
            relativePath = relativePath.substring(1);
            // relativePath = "." + relativePath;
        }
        return relativePath;
    }

    /**
     * Gets a relative path you can use to refer to the context root.
     *
     * @param request the request for which the location should be determined
     * @return A relative path to the context root. Never ends with a slash (/).
     */
    @Override
    public String getContextRootRelativePath(VaadinRequest request) {
        return getCancelingRelativePath("/") + "/";
    }

    // Just to avoid direct calls to VaadinServletService
    // from outside VertxVaadinService
    public static String getCancelingRelativePath(String servletPath) {
        return HandlerHelper.getCancelingRelativePath(servletPath);
    }
}
