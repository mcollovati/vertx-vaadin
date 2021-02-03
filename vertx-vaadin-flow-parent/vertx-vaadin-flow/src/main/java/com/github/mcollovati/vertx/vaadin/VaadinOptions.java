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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_PRODUCTION_MODE;
import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_PUSH_URL;
import static io.vertx.ext.web.handler.SessionHandler.DEFAULT_SESSION_TIMEOUT;

public final class VaadinOptions {


    private final JsonObject config;

    public VaadinOptions() {
        this(new JsonObject());
    }

    public VaadinOptions(JsonObject config) {
        this.config = config;
    }

    public boolean debug() {
        return config.getBoolean("debug", false);
    }

    public boolean productionMode() {
        return config.getBoolean(SERVLET_PARAMETER_PRODUCTION_MODE, false);
    }

    public String mountPoint() {
        return config.getString("mountPoint", "");
    }

    public String connectEndpoint() {
        return config.getString("endpoint.prefix", "/connect");
    }

    public String pushURL() {
        String pushURL = config.getString(SERVLET_PARAMETER_PUSH_URL, "");
        if (pushURL.startsWith(mountPoint())) {
            return pushURL.substring(mountPoint().length());
        }
        return pushURL;
    }

    public String sessionCookieName() {
        return config.getString("sessionCookieName", "vertx-web.session");
    }

    public long sessionTimeout() {
        return config.getLong("sessionTimeout", DEFAULT_SESSION_TIMEOUT);
    }

    public Optional<String> serviceName() {
        return Optional.ofNullable(config.getString("serviceName"));
    }

    public List<String> flowBasePackages() {
        return config.getJsonArray("flowBasePackages", new JsonArray())
            .stream().filter(String.class::isInstance).map(String.class::cast)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Properties asProperties() {
        Properties initParameters = new Properties();
        initParameters.putAll((Map<String, Object>) adaptJson(config.getMap()));
        return initParameters;
    }

    String getParameter(String name) {
        return config.getString(name);
    }

    void sockJSSupport(boolean enabled) {
        config.put("sockJSSupport", enabled);
    }

    long sockJSHeartbeatInterval() {
        return config.getLong("sockJS.heartbeatInterval", 25L * 1000);
    }

    public boolean supportsSockJS() {
        return config.getBoolean("sockJSSupport", true);
    }

    @SuppressWarnings("unchecked")
    private Object adaptJson(Object object) {
        if (object instanceof Collection) {
            return ((Collection<?>) object).stream()
                .map(this::adaptJson)
                .collect(Collectors.toList());
        } else if (object instanceof Map) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>((Map<String, Object>) object);
            map.replaceAll((k, v) -> adaptJson(v));
            return map;
        } else if (object instanceof JsonObject) {
            return adaptJson(((JsonObject) object).getMap());
        } else if (object instanceof JsonArray) {
            return adaptJson(((JsonArray) object).getList());
        } else if (object instanceof String) {
            return object;
        } else if (object != null) {
            return Json.encode(object);
        }
        return object;
    }

}
