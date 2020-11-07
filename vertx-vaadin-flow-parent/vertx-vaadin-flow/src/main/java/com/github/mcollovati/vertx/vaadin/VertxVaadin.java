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

import javax.servlet.ServletContext;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import com.github.mcollovati.vertx.Sync;
import com.github.mcollovati.vertx.http.HttpReverseProxy;
import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSLiveReload;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushConnection;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.SockJSPushHandler;
import com.github.mcollovati.vertx.web.sstore.ExtendedLocalSessionStore;
import com.github.mcollovati.vertx.web.sstore.ExtendedSessionStore;
import com.github.mcollovati.vertx.web.sstore.NearCacheSessionStore;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.server.Constants;
import com.vaadin.flow.server.DevModeHandler;
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

public class VertxVaadin {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VertxVaadin.class);
    private static final String VAADIN_SESSION_EXPIRED_ADDRESS = "vaadin.session.expired";
    private static final String VERSION;

    private final VertxVaadinService service;
    private final StartupContext startupContext;
    private final VaadinOptions config;
    private final Vertx vertx;
    private final Router router;
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

        service = createVaadinService();

        if (config.supportsSockJS()) {
            logger.trace("Configuring SockJS Push connection");
            service.addUIInitListener(event ->
                event.getUI().getInternals().setPushConnection(new SockJSPushConnection(event.getUI()))
            );
        }

        logger.trace("Setup WebJar server");
        try {
            service.init();
        } catch (Exception ex) {
            throw new VertxException("Cannot initialize Vaadin service", ex);
        }

        this.sessionStore = withSessionExpirationHandler(
            service, sessionStore.orElseGet(this::createSessionStore)
        );
        configureSessionStore();
        router = initRouter();
    }

    protected VertxVaadin(Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext) {
        this(vertx, Optional.of(sessionStore), startupContext);
    }

    protected VertxVaadin(Vertx vertx, StartupContext startupContext) {
        this(vertx, Optional.empty(), startupContext);
    }

    private void configureSessionStore() {
        final Registration sessionInitListenerReg = service.addSessionInitListener(event -> {
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
        service.addServiceDestroyListener(event -> sessionInitListenerReg.remove());
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
        return config.serviceName().orElseGet(() -> getClass().getName() + ".service");
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

    private Router initRouter() {
        logger.debug("Initializing router");
        String sessionCookieName = sessionCookieName();
        SessionHandler sessionHandler = SessionHandler.create(sessionStore)
            .setSessionTimeout(config().sessionTimeout())
            .setSessionCookieName(sessionCookieName)
            .setNagHttps(false)
            .setCookieHttpOnlyFlag(true);

        Router vaadinRouter = Router.router(vertx);
        // Redirect mountPoint to mountPoint/
        vaadinRouter.routeWithRegex("^$").handler(ctx -> ctx.response()
            .putHeader(HttpHeaders.LOCATION, ctx.request().uri() + "/")
            .setStatusCode(302).end()
        );

        vaadinRouter.route().handler(BodyHandler.create());

        // Disable SessionHandler for /VAADIN/ static resources
        vaadinRouter.routeWithRegex("^(?!/(VAADIN(?!/dynamic)|frontend|frontend-es6|webjars|webroot)/).*$")
            .handler(sessionHandler);

        // Serve push javascript
        StaticHandler metaInfVaadinStatic = StaticHandler.create("META-INF/resources/VAADIN/static");

        String pushJavascript = String.format(
            "META-INF/resources/VAADIN/static/push/vaadinPush%s",
            config.supportsSockJS() ? "SockJS" : ""
        );
        vaadinRouter.routeWithRegex("/VAADIN/static/push/vaadinPush(SockJS)?(?<suffix>.*)")
            .handler(ctx -> ctx.response().sendFile(pushJavascript + ctx.request().getParam("suffix")));

        DevModeHandler devModeHandler = DevModeHandler.getDevModeHandler();
        if (devModeHandler != null) {
            logger.info("Starting DevModeHandler proxy");
            HttpReverseProxy proxy = HttpReverseProxy.create(vertx, devModeHandler);
            //vaadinRouter.routeWithRegex(".+\\.js$").blockingHandler(proxy::forward);
            vaadinRouter.routeWithRegex("/VAADIN/(?!static/push/).+\\.js$").blockingHandler(proxy::forward);
        }

        //
        vaadinRouter.route("/VAADIN/static/client/*")
            .handler(StaticHandler.create("META-INF/resources/VAADIN/static/client"));
        vaadinRouter.route("/VAADIN/build/*").handler(StaticHandler.create("META-INF/VAADIN/build"));
        vaadinRouter.route("/VAADIN/static/*").handler(StaticHandler.create("VAADIN/static"));
        vaadinRouter.route("/VAADIN/static/*").handler(metaInfVaadinStatic);
        vaadinRouter.routeWithRegex("/VAADIN(?!/dynamic)/.*").handler(StaticHandler.create("VAADIN"));
        vaadinRouter.route("/webroot/*").handler(StaticHandler.create("webroot"));
        vaadinRouter.route("/webjars/*").handler(StaticHandler.create("webroot"));
        vaadinRouter.route("/webjars/*").handler(StaticHandler.create("META-INF/resources/webjars"));

        vaadinRouter.routeWithRegex("/frontend/bower_components/(?<webjar>.*)").handler(ctx -> {
                logger.trace("Rerouting bower component to {}/webjars/{}",
                    ctx.mountPoint(), Objects.toString(ctx.request().getParam("webjar"), "")
                );
                ctx.reroute(String.format("%s/webjars/%s",
                    ctx.mountPoint(), Objects.toString(ctx.request().getParam("webjar"), "")
                ));
            }
        );

        logger.trace("Setup fronted routes");
        if (shouldFixIncorrectWebjarPaths()) {
            vaadinRouter.routeWithRegex(INCORRECT_WEBJAR_PATH_REGEX.pattern() + ".*").handler(ctx -> {
                ctx.reroute(fixIncorrectWebjarPath(ctx.request().path()));
            });
        }

        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("frontend"));
        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("webroot"));
        vaadinRouter.route("/frontend/*").handler(StaticHandler.create("META-INF/resources/frontend"));
        vaadinRouter.route("/frontend-es6/*").handler(StaticHandler.create("frontend-es6"));
        vaadinRouter.route("/frontend-es6/*").handler(StaticHandler.create("META-INF/resources/frontend-es6"));

        initSockJS(vaadinRouter, sessionHandler);

        vaadinRouter.route("/*").handler(StaticHandler.create("META-INF/resources"));
        vaadinRouter.route("/*").blockingHandler(this::handleVaadinRequest);

        serviceInitialized(vaadinRouter);
        return vaadinRouter;
    }

    private void handleVaadinRequest(RoutingContext routingContext) {
        CurrentInstance.clearAll();
        VertxVaadinRequest request = new VertxVaadinRequest(service, routingContext);
        VertxVaadinResponse response = new VertxVaadinResponse(service, routingContext);
        try {
            logger.trace("Handling Vaadin request: {}", routingContext.request().uri());
            service.handleRequest(request, response);
            logger.trace("Vaadin request completed: {}", routingContext.request().uri());
            response.end();
        } catch (Exception ex) {
            logger.error("Error processing request {}", routingContext.request().uri(), ex);
            routingContext.fail(ex);
        }
    }

    private void initSockJS(Router vaadinRouter, SessionHandler sessionHandler) {
        if (config.supportsSockJS()) {

            SockJSHandlerOptions options = new SockJSHandlerOptions()
                .setSessionTimeout(config.sessionTimeout())
                .setHeartbeatInterval(config.sockJSHeartbeatInterval());
            SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

            SockJSPushHandler pushHandler = new SockJSPushHandler(service, sessionHandler, sockJSHandler);

            String pushPath = config.pushURL().replaceFirst("/$", "") + "/*";
            logger.debug("Setup PUSH communication on {}", pushPath);

            service.getContext().setAttribute(SockJSLiveReload.class, pushHandler);

            vaadinRouter.route(pushPath).handler(rc -> {
                if (ApplicationConstants.REQUEST_TYPE_PUSH.equals(rc.request().getParam(ApplicationConstants.REQUEST_TYPE_PARAMETER))) {
                    rc.put(ApplicationConstants.REQUEST_TYPE_PUSH, true);
                    if (rc.request().params().contains(ApplicationConstants.LIVE_RELOAD_CONNECTION)) {
                        rc.put(ApplicationConstants.LIVE_RELOAD_CONNECTION, true);
                        rc.reroute(rc.request().method(), rc.request().path() + "/websocket");
                        return;
                    }
                }
                rc.next();
            });
            vaadinRouter.route(pushPath).handler(rc -> {
                //if (ApplicationConstants.REQUEST_TYPE_PUSH.equals(rc.request().getParam(ApplicationConstants.REQUEST_TYPE_PARAMETER))) {
                if (Boolean.TRUE.equals(rc.get(ApplicationConstants.REQUEST_TYPE_PUSH))) {
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

    private DeploymentConfiguration createDeploymentConfiguration() {
        return DeploymentConfigurationFactory.createDeploymentConfiguration(getClass(), config());
    }


    public static VertxVaadin create(Vertx vertx, ExtendedSessionStore sessionStore, StartupContext startupContext) {
        return new VertxVaadin(vertx, sessionStore, startupContext);
    }

    public static VertxVaadin create(Vertx vertx, JsonObject config) {
        StartupContext startupContext = Sync.await(completer -> StartupContext.of(vertx, new VaadinOptions(config)).setHandler(completer));
        return create(vertx, startupContext);
    }

    public static VertxVaadin create(Vertx vertx, StartupContext config) {
        return new VertxVaadin(vertx, config);
    }

    private static ExtendedSessionStore withSessionExpirationHandler(
        VertxVaadinService service, ExtendedSessionStore store
    ) {
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
    static final String PROPERTY_FIX_INCORRECT_WEBJAR_PATHS = Constants.VAADIN_PREFIX
        + "fixIncorrectWebjarPaths";
    private static final Pattern INCORRECT_WEBJAR_PATH_REGEX = Pattern
        .compile("^/frontend[-\\w/]*/webjars/");

    private boolean shouldFixIncorrectWebjarPaths() {
        return service.getDeploymentConfiguration().isProductionMode()
            && service.getDeploymentConfiguration().getBooleanProperty(
            PROPERTY_FIX_INCORRECT_WEBJAR_PATHS, false);
    }

    private String fixIncorrectWebjarPath(String requestFilename) {
        return INCORRECT_WEBJAR_PATH_REGEX.matcher(requestFilename)
            .replaceAll("/webjars/");
    }

}
