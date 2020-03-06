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
package com.github.mcollovati.vertx.vaadin.communication;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.RequestHandler;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link StreamResource} and {@link StreamReceiver} instances
 * registered in {@link VaadinSession}.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public class VertxStreamRequestHandler implements RequestHandler {

    private static final char PATH_SEPARATOR = '/';

    /**
     * Dynamic resource URI prefix.
     */
    static final String DYN_RES_PREFIX = "VAADIN/dynamic/resource/";

    private StreamResourceHandler resourceHandler = new StreamResourceHandler();
    private StreamReceiverHandler receiverHandler = new StreamReceiverHandler();

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
                                 VaadinResponse response) throws IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            return false;
        }
        // remove leading '/'
        assert pathInfo.startsWith(Character.toString(PATH_SEPARATOR));
        pathInfo = pathInfo.substring(1);

        if (!pathInfo.startsWith(DYN_RES_PREFIX)) {
            return false;
        }

        Optional<AbstractStreamResource> abstractStreamResource;
        session.lock();
        try {
            abstractStreamResource = VertxStreamRequestHandler.getPathUri(pathInfo)
                .flatMap(session.getResourceRegistry()::getResource);
            if (!abstractStreamResource.isPresent()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Resource is not found for path=" + pathInfo);
                return true;
            }
        } finally {
            session.unlock();
        }

        if (abstractStreamResource.isPresent()) {
            AbstractStreamResource resource = abstractStreamResource.get();
            if (resource instanceof StreamResource) {
                resourceHandler.handleRequest(session, request, response,
                    (StreamResource) resource);
            } else if (resource instanceof StreamReceiver) {
                StreamReceiver streamReceiver = (StreamReceiver) resource;
                String[] parts = parsePath(pathInfo);

                receiverHandler.handleRequest(session, request, response,
                    streamReceiver, parts[0], parts[1]);
            } else {
                getLogger().warn("Received unknown stream resource.");
            }
        }
        return true;
    }

    /**
     * Parse the pathInfo for id data.
     * s   * <p>
     * URI pattern: VAADIN/dynamic/resource/[UIID]/[SECKEY]/[NAME]
     *
     * @see #generateURI
     */
    private String[] parsePath(String pathInfo) {
        // strip away part until the data we are interested starts
        int startOfData = pathInfo.indexOf(DYN_RES_PREFIX)
            + DYN_RES_PREFIX.length();

        String uppUri = pathInfo.substring(startOfData);
        // [0] UIid, [1] security key, [2] name
        return uppUri.split("/", 3);
    }

    /**
     * Generates URI string for a dynamic resource using its {@code id} and
     * {@code name}. [0] UIid, [1] sec key, [2] name
     *
     * @param name file or attribute name to use in path
     * @param id   unique resource id
     * @return generated URI string
     */
    public static String generateURI(String name, String id) {
        StringBuilder builder = new StringBuilder(DYN_RES_PREFIX);

        try {
            builder.append(UI.getCurrent().getUIId()).append(PATH_SEPARATOR);
            builder.append(id).append(PATH_SEPARATOR);
            builder.append(
                URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            // UTF8 has to be supported
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    private static Optional<URI> getPathUri(String path) {
        int index = path.lastIndexOf('/');
        boolean hasPrefix = index >= 0;
        if (!hasPrefix) {
            getLogger().info("Unsupported path structure, path={}", path);
            return Optional.empty();
        }
        String prefix = path.substring(0, index + 1);
        String name = path.substring(prefix.length());
        // path info returns decoded name but space ' ' remains encoded '+'
        name = name.replace('+', ' ');
        try {
            URI uri = new URI(prefix
                + URLEncoder.encode(name, StandardCharsets.UTF_8.name()));
            return Optional.of(uri);
        } catch (UnsupportedEncodingException e) {
            // UTF8 has to be supported
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            getLogger().info("Path '{}' is not correct URI (it violates RFC 2396)", path, e);
            return Optional.empty();
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(StreamResourceHandler.class.getName());
    }
}
