package com.github.mcollovati.vertx.vaadin;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.MimeMapping;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;

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
        return Optional.ofNullable(routingContext.mountPoint())
                .orElse("")
                .replaceAll("/$", "");
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
