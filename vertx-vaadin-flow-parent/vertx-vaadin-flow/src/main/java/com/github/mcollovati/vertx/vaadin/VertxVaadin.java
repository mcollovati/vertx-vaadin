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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushConnection;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushHandler;
import com.github.mcollovati.vertx.web.sstore.ExtendedLocalSessionStore;
import com.github.mcollovati.vertx.web.sstore.ExtendedSessionStore;
import com.github.mcollovati.vertx.web.sstore.NearCacheSessionStore;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.DefaultDeploymentConfiguration;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.shared.ApplicationConstants;
import com.vaadin.flow.shared.Registration;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import static io.vertx.ext.web.handler.SessionHandler.DEFAULT_SESSION_TIMEOUT;

public class VertxVaadin {

    private static final String VAADIN_SESSION_EXPIRED_ADDRESS = "vaadin.session.expired";
    private static final String VERSION;

    private final VertxVaadinService service;
    private final JsonObject config;
    private final Vertx vertx;
    private final Router router;
    private final ExtendedSessionStore sessionStore;
    WebJars webJars;

    static {
        String version = "0.0.0";
        Properties properties = new Properties();
        try {
            properties.load(VertxVaadin.class.getResourceAsStream("version.properties"));
            version = properties.getProperty("vertx-vaadin.version");
        } catch (Exception e) {
            getLogger().warning("Unable to determine VertxVaadin version");
        }
        VERSION = version;
    }


    private VertxVaadin(Vertx vertx, Optional<ExtendedSessionStore> sessionStore, JsonObject config) {
        this.vertx = Objects.requireNonNull(vertx);
        this.config = Objects.requireNonNull(config);

        this.service = createVaadinService();
        this.service.addUIInitListener(event ->
            event.getUI().getInternals().setPushConnection(new SockJSPushConnection(event.getUI()))
        );
        this.webJars = new WebJars(service.getDeploymentConfiguration());
        try {
            service.init();
        } catch (Exception ex) {
            throw new VertxException("Cannot initialize Vaadin service", ex);
        }

        //SessionStore adaptedSessionStore = SessionStoreAdapter.adapt(service, sessionStore.orElseGet(this::createSessionStore));
        this.sessionStore = withSessionExpirationHandler(
            this.service, sessionStore.orElseGet(this::createSessionStore)
        );
        configureSessionStore();
        this.router = initRouter();
    }

    protected VertxVaadin(Vertx vertx, ExtendedSessionStore sessionStore, JsonObject config) {
        this(vertx, Optional.of(sessionStore), config);
    }

    protected VertxVaadin(Vertx vertx, JsonObject config) {
        this(vertx, Optional.empty(), config);
    }

    private void configureSessionStore() {
        final Registration sessionInitListenerReg = this.service.addSessionInitListener(event -> {
            MessageConsumer<String> consumer = sessionExpiredHandler(vertx, msg ->
                Optional.of(event.getSession().getSession())
                    .filter(session -> msg.body().equals(session.getId()))
                    .ifPresent(WrappedSession::invalidate));
            AtomicReference<Registration> sessionDestroyListenerUnregister = new AtomicReference<>();
            sessionDestroyListenerUnregister.set(
                event.getService().addSessionDestroyListener(ev2 -> {
                    consumer.unregister();
                    sessionDestroyListenerUnregister.get().remove();
                })
            );

        });
        this.service.addServiceDestroyListener(event -> sessionInitListenerReg.remove());
    }

    public Router router() {
        return router;
    }

    public final Vertx vertx() {
        return vertx;
    }

    public final VertxVaadinService vaadinService() {
        return service;
    }

    public String serviceName() {
        return config.getString("serviceName", getClass().getName() + ".service");
    }


    protected final JsonObject config() {
        return config;
    }

    protected void serviceInitialized(Router router) {
        // empty by default
    }

    protected VertxVaadinService createVaadinService() {
        VertxVaadinService service = new VertxVaadinService(this, createDeploymentConfiguration());
        return service;
    }

    protected ExtendedSessionStore createSessionStore() {
        if (vertx.isClustered()) {
            return NearCacheSessionStore.create(vertx);
        }
        return ExtendedLocalSessionStore.create(vertx);
    }

    private Router initRouter() {

        String sessionCookieName = sessionCookieName();
        SessionHandler sessionHandler = SessionHandler.create(sessionStore)
            .setSessionTimeout(config().getLong("sessionTimeout", DEFAULT_SESSION_TIMEOUT))
            .setSessionCookieName(sessionCookieName)
            .setNagHttps(false)
            .setCookieHttpOnlyFlag(true);

        Router vaadinRouter = Router.router(vertx);
        // Redirect mountPoint to mountPoint/
        vaadinRouter.routeWithRegex("^$").handler(ctx -> ctx.response()
            .putHeader(HttpHeaders.LOCATION, ctx.request().uri() + "/")
            .setStatusCode(302).end()
        );

        vaadinRouter.route().handler(CookieHandler.create());
        vaadinRouter.route().handler(BodyHandler.create());

        // Disable SessionHandler for /VAADIN/ static resources
        vaadinRouter.routeWithRegex("^(?!/(VAADIN(?!/dynamic)|frontend|frontend-es6|webjars|webroot)/).*$").handler(sessionHandler);

        // Forward vaadinPush javascript to sockjs implementation
        vaadinRouter.routeWithRegex("/VAADIN/static/push/vaadinPush(?<min>-min)?\\.js(?<compressed>\\.gz)?")
            .handler(ctx -> ctx.reroute(
                String.format("%s/VAADIN/static/push/vaadinPushSockJS%s.js%s", ctx.mountPoint(),
                    Objects.toString(ctx.request().getParam("min"), ""),
                    Objects.toString(ctx.request().getParam("compressed"), "")
                )
            ));

        //
        //vaadinRouter.route("/VAADIN/dynamic/*").handler(this::handleVaadinRequest);
        vaadinRouter.route("/VAADIN/static/client/*")
            .handler(StaticHandler.create("META-INF/resources/VAADIN/static/client", getClass().getClassLoader()));
        vaadinRouter.route("/VAADIN/static/*").handler(StaticHandler.create("VAADIN/static", getClass().getClassLoader()));
        vaadinRouter.route("/VAADIN/static/*").handler(StaticHandler.create("META-INF/resources/VAADIN/static", getClass().getClassLoader()));
        vaadinRouter.routeWithRegex("/VAADIN(?!/dynamic)/.*").handler(StaticHandler.create("VAADIN", getClass().getClassLoader()));
        vaadinRouter.route("/webroot/*").handler(StaticHandler.create("webroot", getClass().getClassLoader()));
        vaadinRouter.route("/webjars/*").handler(StaticHandler.create("webroot", getClass().getClassLoader()));
        vaadinRouter.route("/webjars/*").handler(StaticHandler.create("META-INF/resources/webjars", getClass().getClassLoader()));
        vaadinRouter.routeWithRegex("/frontend/bower_components/(?<webjar>.*)").handler(ctx -> {
                getLogger().fine("============ Rerouting to " + String.format("%s/webjars/%s",
                    ctx.mountPoint(), Objects.toString(ctx.request().getParam("webjar"), "")
                ));
                ctx.reroute(String.format("%s/webjars/%s",
                    ctx.mountPoint(), Objects.toString(ctx.request().getParam("webjar"), "")
                ));
            }
        );
        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("frontend", getClass().getClassLoader()));
        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("webroot", getClass().getClassLoader()));
        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("META-INF/resources/frontend", getClass().getClassLoader()));
        vaadinRouter.route("/frontend-es6/*").handler(StaticHandler.create("frontend-es6", getClass().getClassLoader()));
        vaadinRouter.route("/frontend-es6/*").handler(StaticHandler.create("META-INF/resources/frontend-es6", getClass().getClassLoader()));

        initSockJS(vaadinRouter, sessionHandler);

        vaadinRouter.route("/*").handler(this::handleVaadinRequest);


        serviceInitialized(vaadinRouter);
        return vaadinRouter;
    }

    private void handleVaadinRequest(RoutingContext routingContext) {
        VertxVaadinRequest request = new VertxVaadinRequest(service, routingContext);
        VertxVaadinResponse response = new VertxVaadinResponse(service, routingContext);

        try {
            service.handleRequest(request, response);
            response.end();
        } catch (ServiceException ex) {
            routingContext.fail(ex);
        }
    }

    private void initSockJS(Router vaadinRouter, SessionHandler sessionHandler) {

        SockJSHandlerOptions options = new SockJSHandlerOptions()
            .setSessionTimeout(config().getLong("sessionTimeout", DEFAULT_SESSION_TIMEOUT))
            .setHeartbeatInterval(service.getDeploymentConfiguration().getHeartbeatInterval() * 1000);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
        SockJSPushHandler pushHandler = new SockJSPushHandler(service, sessionHandler, sockJSHandler);
        //vaadinRouter.route("/PUSH/*").handler(pushHandler);
        vaadinRouter.route("/*").handler(rc -> {
            if (ApplicationConstants.REQUEST_TYPE_PUSH.equals(rc.request().getParam(ApplicationConstants.REQUEST_TYPE_PARAMETER))) {
                pushHandler.handle(rc);
            } else {
                rc.next();
            }
        });
    }


    private String sessionCookieName() {
        return config().getString("sessionCookieName", "vertx-web.session");
    }

    private DefaultDeploymentConfiguration createDeploymentConfiguration() {
        return new DefaultDeploymentConfiguration(getClass(), initProperties());
    }

    private Properties initProperties() {
        Properties initParameters = new Properties();
        //initParameters.putAll(config().getMap());
        initParameters.putAll((Map) adaptJson(config().getMap()));
        return initParameters;
    }

    private Object adaptJson(Object object) {
        if (object instanceof Collection) {
            return ((Collection<?>) object).stream()
                .map(this::adaptJson)
                .collect(Collectors.toList());
        } else if (object instanceof Map) {
            LinkedHashMap map = new LinkedHashMap((Map) object);
            map.replaceAll((k, v) -> adaptJson(v));
            return map;
        } else if (object instanceof JsonObject) {
            return adaptJson(((JsonObject) object).getMap());
        } else if (object instanceof JsonArray) {
            return adaptJson(((JsonArray) object).getList());
        }
        return object;
    }

    // TODO: change JsonObject to VaadinOptions interface
    public static VertxVaadin create(Vertx vertx, ExtendedSessionStore sessionStore, JsonObject config) {
        return new VertxVaadin(vertx, sessionStore, config);
    }

    public static VertxVaadin create(Vertx vertx, JsonObject config) {
        return new VertxVaadin(vertx, config);
    }

    private static final Logger getLogger() {
        return Logger.getLogger(VertxVaadin.class.getName());
    }

    private static ExtendedSessionStore withSessionExpirationHandler(
        VertxVaadinService service, ExtendedSessionStore store
    ) {
        MessageProducer<String> sessionExpiredProducer = sessionExpiredProducer(service);
        store.expirationHandler(res -> {
            if (res.succeeded()) {
                sessionExpiredProducer.send(res.result());
            } else {
                res.cause().printStackTrace();
            }
        });
        return store;
    }

    private static MessageProducer<String> sessionExpiredProducer(VertxVaadinService vaadinService) {
        return vaadinService.getVertx().eventBus().sender(VAADIN_SESSION_EXPIRED_ADDRESS);
    }

    public static MessageConsumer<String> sessionExpiredHandler(Vertx vertx, Handler<Message<String>> handler) {
        return vertx.eventBus().consumer(VAADIN_SESSION_EXPIRED_ADDRESS, handler);
    }

    public static String getVersion() {
        return VERSION;
    }

    static final class WebJars {

        static final String WEBJARS_RESOURCES_PREFIX = "META-INF/resources/webjars/";
        private final String webjarsLocation;
        private final Pattern urlPattern;

        private WebJars(DeploymentConfiguration deploymentConfiguration) {
            String frontendPrefix = deploymentConfiguration
                .getDevelopmentFrontendPrefix();
            if (!frontendPrefix.endsWith("/")) {
                throw new IllegalArgumentException(
                    "Frontend prefix must end with a /. Got \"" + frontendPrefix
                        + "\"");
            }
            if (!frontendPrefix
                .startsWith(ApplicationConstants.CONTEXT_PROTOCOL_PREFIX)) {
                throw new IllegalArgumentException(
                    "Cannot host WebJars for a fronted prefix that isn't relative to 'context://'. Current frontend prefix: "
                        + frontendPrefix);
            }

            webjarsLocation = "/"
                + frontendPrefix.substring(
                ApplicationConstants.CONTEXT_PROTOCOL_PREFIX.length());


            urlPattern = Pattern.compile("^((/\\.)?(/\\.\\.)*)" + webjarsLocation + "(bower_components/)?(?<webjar>.*)");
        }


        /**
         * Gets web jar resource path if it exists.
         *
         * @param filePathInContext servlet context path for file
         * @return an optional web jar resource path, or an empty optional if the
         * resource is not web jar resource
         */
        public Optional<String> getWebJarResourcePath(String filePathInContext) {
            String webJarPath = null;

            Matcher matcher = urlPattern.matcher(filePathInContext);
            // If we don't find anything then we don't have the prefix at all.
            if (matcher.find()) {
                webJarPath = WEBJARS_RESOURCES_PREFIX + matcher.group("webjar");
            }
            return Optional.ofNullable(webJarPath);
        }


    }


}
