/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;

public class HttpUtils {

    public static String contextPath(RoutingContext routingContext) {
        // until vertx 3.8.3 routingContext.mountPoint() for "/" is "", since 3.84 it is "/"
        return extractContextPath(routingContext);
    }

    public static String pathInfo(RoutingContext routingContext) {
        String path = routingContext.request().path();
        if (path != null) {
            String pathInfo = path.substring(contextPath(routingContext).length());
            try {
                return URLDecoder.decode(pathInfo, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // fallback to path
                return pathInfo;
            }
        }
        return "";
    }

    public static String extractContextPath(RoutingContext routingContext) {
        // until vertx 3.8.3 routingContext.mountPoint() for "/" is "", since 3.8.4 it is "/"
        return Optional.ofNullable(routingContext.mountPoint()).orElse("").replaceAll("/$", "");
    }

    public static long getDateHeader(String name, HttpServerRequest request) {
        return Utils.parseRFC1123DateTime(request.getHeader(name));
    }

    public static long getDateHeader(CharSequence name, HttpServerRequest request) {
        return getDateHeader(name.toString(), request);
    }

    public static String formatDateHeader(long time) {
        return Utils.formatRFC1123DateTime(time);
    }

    public static long parseDateHeader(String time) {
        return Utils.parseRFC1123DateTime(time);
    }

    public static String getMimeType(String resourceName) {
        return MimeMapping.getMimeTypeForFilename(resourceName);
    }
}
