package com.github.mcollovati.vertx.vaadin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.frontend.FrontendUtils;
import elemental.json.impl.JsonUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;

import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_COMPATIBILITY_MODE;
import static com.vaadin.flow.server.Constants.SERVLET_PARAMETER_PRODUCTION_MODE;
import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_TOKEN_FILE;
import static com.vaadin.flow.server.frontend.FrontendUtils.PROJECT_BASEDIR;
import static com.vaadin.flow.server.frontend.FrontendUtils.TOKEN_FILE;
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

    public boolean compatibilityMode() {
        return config.getBoolean(SERVLET_PARAMETER_COMPATIBILITY_MODE, false);
    }

    public String mountPoint() {
        return config.getString("mountPoint", "");
    }

    public String pushURL() {
        return config.getString(Constants.SERVLET_PARAMETER_PUSH_URL, "");
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
        }
        return object;
    }

}
