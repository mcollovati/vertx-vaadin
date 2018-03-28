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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.vaadin.server.DefaultDeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.ServletPortletHelper;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.communication.ServletUIInitHandler;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.file.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by marco on 16/07/16.
 */
public class VertxVaadinService extends VaadinService {

    private static final Logger logger = LoggerFactory.getLogger(VertxVaadinService.class);

    private final VertxVaadin vertxVaadin;

    public VertxVaadinService(VertxVaadin vertxVaadin, DefaultDeploymentConfiguration deploymentConfiguration) {
        super(deploymentConfiguration);
        this.vertxVaadin = vertxVaadin;
    }

    public Vertx getVertx() {
        return vertxVaadin.vertx();
    }

    @Override
    protected List<RequestHandler> createRequestHandlers()
        throws ServiceException {
        List<RequestHandler> handlers = super.createRequestHandlers();
        handlers.add(0, new VertxBootstrapHandler());
        handlers.add(new ServletUIInitHandler());
        return handlers;
    }

    @Override
    public boolean ensurePushAvailable() {
        return true;
    }

    @Override
    public String getStaticFileLocation(VaadinRequest request) {
        String staticFileLocation;
        // if property is defined in configurations, use that
        staticFileLocation = getDeploymentConfiguration().getResourcesPath();
        if (staticFileLocation != null) {
            return staticFileLocation;
        }

        VertxVaadinRequest vertxRequest = (VertxVaadinRequest) request;
        String requestedPath = vertxRequest.getRequest().path()
            .substring(
                Optional.ofNullable(vertxRequest.getRoutingContext().mountPoint())
                    .map(String::length).orElse(0)
            );
        return VaadinServletService.getCancelingRelativePath(requestedPath);
    }

    @Override
    public String getConfiguredWidgetset(VaadinRequest request) {
        return getDeploymentConfiguration().getWidgetset(VaadinServlet.DEFAULT_WIDGETSET);
    }

    @Override
    public String getConfiguredTheme(VaadinRequest request) {
        return ValoTheme.THEME_NAME;
    }

    @Override
    public boolean isStandalone(VaadinRequest request) {
        return true;
    }

    @Override
    public String getMimeType(String resourceName) {
        return null;
    }

    // TODO: from system property?
    @Override
    public File getBaseDirectory() {
        return new File(".");
    }

    // From VaadinServletService
    @Override
    protected boolean requestCanCreateSession(VaadinRequest request) {
        if (ServletUIInitHandler.isUIInitRequest(request)) {
            // This is the first request if you are embedding by writing the
            // embedding code yourself
            return true;
        } else if (isOtherRequest(request)) {
            /*
             * I.e URIs that are not RPC calls or static (theme) files.
             */
            return true;
        }

        return false;
    }

    // From VaadinServletService
    private boolean isOtherRequest(VaadinRequest request) {
        // TODO This should be refactored in some way. It should not be
        // necessary to check all these types.
        return (!ServletPortletHelper.isAppRequest(request)
            && !ServletUIInitHandler.isUIInitRequest(request)
            && !ServletPortletHelper.isFileUploadRequest(request)
            && !ServletPortletHelper.isHeartbeatRequest(request)
            && !ServletPortletHelper.isPublishedFileRequest(request)
            && !ServletPortletHelper.isUIDLRequest(request) && !ServletPortletHelper
            .isPushRequest(request));
    }

    @Override
    public String getServiceName() {
        return vertxVaadin.serviceName();
    }

    @Override
    public InputStream getThemeResourceAsStream(UI uI, String themeName, String resource) {
        String filename = VaadinServlet.THEME_DIR_PATH + '/' + themeName
            + "/" + resource;

        String normalized = Paths.get(filename).normalize().toString();
        if (!normalized.startsWith("VAADIN/")
            || normalized.contains("/../")) {
            throw new VertxException(String.format(
                "Requested resource [%s] not accessible in the VAADIN directory or access to it is forbidden.",
                filename));
        }
        FileSystem fileSystem = getVertx().fileSystem();
        if (fileSystem.existsBlocking(filename)) {

            return new ByteArrayInputStream(fileSystem.readFileBlocking(filename).getBytes());
        }
        return null;
    }


    // Adapted from VaadinServletService
    @Override
    public String getMainDivId(VaadinSession session, VaadinRequest request, Class<? extends UI> uiClass) {
        String appId = request.getPathInfo();
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

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * Gets a relative path you can use to refer to the context root.
     *
     * @param request the request for which the location should be determined
     * @return A relative path to the context root. Never ends with a slash (/).
     */
    public static String getContextRootRelativePath(VaadinRequest request) {
        VertxVaadinRequest servletRequest = (VertxVaadinRequest) request;
        // Generate location from the request by finding how many "../" should
        // be added to the servlet path before we get to the context root

        String servletPath = "";

        String pathInfo = servletRequest.getPathInfo();
        if (pathInfo != null && !pathInfo.isEmpty()) {
            servletPath += pathInfo;
        }

        return getCancelingRelativePath(servletPath);
    }

    // Just to avoid direct calls to VaadinServletService
    // from outside VertxVaadinService
    public static String getCancelingRelativePath(String servletPath) {
        return VaadinServletService.getCancelingRelativePath(servletPath);
    }

}
