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
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.ApplicationClassLoaderAccess;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.internal.VaadinContextInitializer;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinServlet;
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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.Sync;
import com.github.mcollovati.vertx.http.HttpReverseProxy;
import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.communication.VertxDebugWindowConnection;
import com.github.mcollovati.vertx.vaadin.connect.VaadinConnectHandler;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushConnection;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushHandler;
import com.github.mcollovati.vertx.web.sstore.ExtendedLocalSessionStore;
import com.github.mcollovati.vertx.web.sstore.ExtendedSessionStore;
import com.github.mcollovati.vertx.web.sstore.NearCacheSessionStore;

public class VertxVaadin {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VertxVaadin.class);
    private static final String VAADIN_SESSION_EXPIRED_ADDRESS = "vaadin.session.expired";
    private static final String VERSION;

    private final VertxVaadinService service;
    private final StartupContext startupContext;
    private final VaadinOptions config;
    private final Vertx vertx;
    private final Router vaadinRouter;
    private final Router connectRouter;
    private final ExtendedSessionStore sessionStore;

    static {
        String version = "0.0.0";
        Properties properties = new Properties();
        try {
            properties.load(VertxVaadin.class.getResourceAsStream("version.properties"));
            version = properties.getProperty("vertx-vaadin.version");
        } catch (Exception e) {
            logger.warn("Unable to determine VertxVaadin version");
        }
        VERSION = version;
    }

    private VertxVaadin(Vertx vertx, Optional<ExtendedSessionStore> sessionStore, StartupContext startupContext) {
        this.vertx = Objects.requireNonNull(vertx);
        this.startupContext = Objects.requireNonNull(startupContext);
        config = startupContext.vaadinOptions();
        CurrentInstance.clearAll();
        try {
            CurrentInstance.set(VertxVaadin.class, this);
            initializeVaadinContext(vertx);
            service = createVaadinService();
            workAroundForDetectFrontendMapping();

            if (config.supportsSockJS()) {
                logger.trace("Configuring SockJS Push connection");
                service.addUIInitListener(event ->
                        event.getUI().getInternals().setPushConnection(new SockJSPushConnection(event.getUI())));
            }

            logger.trace("Setup WebJar server");
            try {
                service.init();
            } catch (Exception ex) {
                throw new VertxException("Cannot initialize Vaadin service", ex);
            }

            this.sessionStore = withSessionExpirationHandler(service, sessionStore.orElseGet(this::createSessionStore));
            configureSessionStore();
            vaadinRouter = Router.router(vertx);
            connectRouter = Router.router(vertx);
            initRouters();
        } finally {
            CurrentInstance.clearAll();
        }
    }

    // DevModeInitializer blocks until VaadinServlet has been initialized and
    // the static frontendMapping field is set.
    // Unfortunately there's not other way to hook into this rather than use
    // reflection.
    private void workAroundForDetectFrontendMapping() {
        try {
            MethodHandles.privateLookupIn(VaadinServlet.class, MethodHandles.lookup())
                    .findStaticVarHandle(VaadinServlet.class, "frontendMapping", String.class)
                    .set(config.mountPoint());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot set VaadinServlet.frontendMapping by reflection. "
                            + "This is necessary to start Vaadin DevModeServer",
                    e);
        }
    }

    protected VertxVaadin(Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext) {
        this(vertx, Optional.of(sessionStore), startupContext);
    }

    protected VertxVaadin(Vertx vertx, StartupContext startupContext) {
        this(vertx, Optional.empty(), startupContext);
    }

    private VaadinContext initializeVaadinContext(Vertx vertx) {
        VertxVaadinContext context = (VertxVaadinContext) startupContext.getVaadinContext();
        ApplicationClassLoaderAccess access = () -> context.getClass().getClassLoader();
        context.getAttribute(ApplicationClassLoaderAccess.class, () -> access);

        VaadinContextInitializer initializer = context.getAttribute(VaadinContextInitializer.class);
        if (initializer != null) {
            initializer.initialize(context);
        }
        return context;
    }

    /**
     * Provides access to {@link StartupContext} during instance creation.
     *
     * @param function {@link StartupContext} instance.
     * @param <T>      the type of the computation result.
     * @return result of applying given function to {@link StartupContext}
     * @throws IllegalStateException if used after instance creation.
     */
    protected final <T> T computeWithStartupContext(Function<StartupContext, T> function) {
        if (CurrentInstance.get(VertxVaadin.class) != this) {
            throw new IllegalStateException("StartupContext cannot be used after VertxVaadin creation");
        }
        return function.apply(startupContext);
    }

    VertxVaadinContext newVaadinContext() {
        return (VertxVaadinContext) startupContext.getVaadinContext();
    }

    private void configureSessionStore() {
        final Registration sessionInitListenerReg = service.addSessionInitListener(event -> {
            MessageConsumer<String> consumer = sessionExpiredHandler(
                    vertx, msg -> Optional.of(event.getSession().getSession())
                            .filter(session -> msg.body().equals(session.getId()))
                            .ifPresent(WrappedSession::invalidate));
            AtomicReference<Registration> sessionDestroyListenerUnregister = new AtomicReference<>();
            sessionDestroyListenerUnregister.set(event.getService().addSessionDestroyListener(ev2 -> {
                consumer.unregister();
                sessionDestroyListenerUnregister.get().remove();
            }));
        });
        service.addServiceDestroyListener(event -> sessionInitListenerReg.remove());
    }

    public Router router() {
        return vaadinRouter;
    }

    public Router connectRouter() {
        return connectRouter;
    }

    public final Vertx vertx() {
        return vertx;
    }

    public final VertxVaadinService vaadinService() {
        return service;
    }

    public String serviceName() {
        return config.serviceName().orElseGet(() -> vertx.getOrCreateContext().deploymentID() + ".service");
    }

    protected final VaadinOptions config() {
        return config;
    }

    ServletContext servletContext() {
        return startupContext.servletContext();
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

    private void initRouters() {
        logger.debug("Initializing router");
        String sessionCookieName = sessionCookieName();
        SessionHandler sessionHandler = SessionHandler.create(sessionStore)
                .setSessionTimeout(config().sessionTimeout())
                .setSessionCookieName(sessionCookieName)
                .setNagHttps(false)
                .setCookieHttpOnlyFlag(true);

        connectRouter.route().handler(sessionHandler);
        connectRouter.route().handler(BodyHandler.create());

        if (config.hillaEnabled()) {
            VaadinConnectHandler.register(connectRouter, service);
        }

        // Redirect mountPoint to mountPoint/
        vaadinRouter.routeWithRegex("^$").handler(ctx -> ctx.response()
                .putHeader(HttpHeaders.LOCATION, ctx.request().uri() + "/")
                .setStatusCode(302)
                .end());

        vaadinRouter.route().handler(BodyHandler.create());

        // Disable SessionHandler for /VAADIN/ static resources
        vaadinRouter
                .routeWithRegex("^(?!/(VAADIN(?!/(dynamic|push))|frontend|frontend-es6|webjars|webroot)/).*$")
                .handler(sessionHandler);

        String pushJavascript = String.format(
                "META-INF/resources/VAADIN/static/push/vaadinPush%s", config.supportsSockJS() ? "SockJS" : "");
        vaadinRouter
                .routeWithRegex("/VAADIN/static/push/vaadinPush(SockJS)?(?<suffix>.*)")
                .handler(ctx ->
                        ctx.response().sendFile(pushJavascript + ctx.request().getParam("suffix")));

        DevModeHandler devModeHandler =
                DevModeHandlerManager.getDevModeHandler(service).orElse(null);
        if (devModeHandler != null && devModeHandler.getPort() >= 0) {
            logger.info("Starting DevModeHandler proxy");
            HttpReverseProxy proxy = HttpReverseProxy.create(vertx, devModeHandler);
            vaadinRouter.routeWithRegex("^/themes\\/[\\s\\S]+?\\/").handler(proxy::forward);
            vaadinRouter.routeWithRegex("/VAADIN(?!/(dynamic|push))/.*").handler(proxy::forward);
            vaadinRouter.routeWithRegex("/.*\\.js").handler(proxy::forward);
            vaadinRouter.routeWithRegex("/(index|web-component)\\.html").handler(proxy::forward);

            VertxDebugWindowConnection windowConnection =
                    service.getContext().getAttribute(VertxDebugWindowConnection.class);
            windowConnection.attachService(service);
        }

        initSockJS(vaadinRouter, sessionHandler);

        VertxStaticFileServer staticFileServer = new VertxStaticFileServer(service);
        vaadinRouter.route("/*").handler(staticFileServer);
        vaadinRouter.routeWithRegex("/.+").handler(StaticHandler.create("META-INF/resources"));
        vaadinRouter.route("/*").blockingHandler(this::handleVaadinRequest);

        serviceInitialized(vaadinRouter);
    }

    private void handleVaadinRequest(RoutingContext routingContext) {
        CurrentInstance.clearAll();
        VertxVaadinRequest request = new VertxVaadinRequest(service, routingContext);
        VertxVaadinResponse response = new VertxVaadinResponse(service, routingContext);
        try {
            logger.trace("Handling Vaadin request: {}", routingContext.request().uri());
            service.handleRequest(request, response);
            logger.trace(
                    "Vaadin request completed: {}", routingContext.request().uri());
            response.end();
        } catch (Exception ex) {
            logger.error("Error processing request {}", routingContext.request().uri(), ex);
            routingContext.fail(ex);
        }
    }

    private void initSockJS(Router vaadinRouter, SessionHandler sessionHandler) {
        if (config.supportsSockJS()) {

            SockJSHandlerOptions options = new SockJSHandlerOptions()
                    .setRegisterWriteHandler(true)
                    .setSessionTimeout(config.sessionTimeout())
                    .setHeartbeatInterval(config.sockJSHeartbeatInterval());
            SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

            SockJSPushHandler pushHandler = new SockJSPushHandler(service, sessionHandler, sockJSHandler);

            String pushPath = config.pushURL().replaceFirst("/$", "") + "/*";
            logger.debug("Setup PUSH communication on {}", pushPath);

            vaadinRouter.route(pushPath).handler(rc -> {
                if (ApplicationConstants.REQUEST_TYPE_PUSH.equals(
                        rc.request().getParam(ApplicationConstants.REQUEST_TYPE_PARAMETER))) {
                    pushHandler.handle(rc);
                } else {
                    rc.next();
                }
            });
        } else {
            logger.info("PUSH communication over SockJS is disabled");
        }
    }

    private String sessionCookieName() {
        return config().sessionCookieName();
    }

    protected DeploymentConfiguration createDeploymentConfiguration() {
        return startupContext.deploymentConfiguration();
        // return new DefaultDeploymentConfiguration(ApplicationConfiguration.get(startupContext.getVaadinContext()),
        // getClass(), config.asProperties());
    }

    public static VertxVaadin create(Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext) {
        return new VertxVaadin(vertx, sessionStore, startupContext);
    }

    public static VertxVaadin create(Vertx vertx, JsonObject config) {
        StartupContext startupContext = Sync.await(
                completer -> StartupContext.of(vertx, new VaadinOptions(config)).onComplete(completer));
        return create(vertx, startupContext);
    }

    public static VertxVaadin create(Vertx vertx, StartupContext config) {
        return new VertxVaadin(vertx, config);
    }

    private static ExtendedSessionStore withSessionExpirationHandler(
            VertxVaadinService service, ExtendedSessionStore store) {
        MessageProducer<String> sessionExpiredProducer = sessionExpiredProducer(service);
        store.expirationHandler(res -> {
            if (res.succeeded()) {
                sessionExpiredProducer.write(res.result());
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

    // When referring to webjar resources from application stylesheets (loaded
    // using @StyleSheet) using relative paths, the paths will be different in
    // development mode and in production mode. The reason is that in production
    // mode, the CSS is incorporated into the bundle and when this happens,
    // the relative paths are changed so that they end up pointing to paths like
    // 'frontend-es6/webjars' instead of just 'webjars'.

    // There is a similar problem when referring to webjar resources from
    // application stylesheets inside HTML custom styles (loaded using
    // @HtmlImport). In this case, the paths will also be changed in production.
    // For example, if the HTML file resides in 'frontend/styles' and refers to
    // 'webjars/foo', the path will be changed to refer to
    // 'frontend/styles/webjars/foo', which is incorrect. You could add '../../'
    // to the path in the HTML file but then it would not work in development
    // mode.

    // These paths are changed deep inside the Polymer build chain. It was
    // easier to fix the StaticFileServer to take the incorrect path names
    // into account than fixing the Polymer build chain to generate correct
    // paths. Hence, these methods:
    static final String PROPERTY_FIX_INCORRECT_WEBJAR_PATHS = Constants.VAADIN_PREFIX + "fixIncorrectWebjarPaths";
    private static final Pattern INCORRECT_WEBJAR_PATH_REGEX = Pattern.compile("^/frontend[-\\w/]*/webjars/");

    private boolean shouldFixIncorrectWebjarPaths() {
        return service.getDeploymentConfiguration().isProductionMode()
                && service.getDeploymentConfiguration().getBooleanProperty(PROPERTY_FIX_INCORRECT_WEBJAR_PATHS, false);
    }

    private String fixIncorrectWebjarPath(String requestFilename) {
        return INCORRECT_WEBJAR_PATH_REGEX.matcher(requestFilename).replaceAll("/webjars/");
    }
}
