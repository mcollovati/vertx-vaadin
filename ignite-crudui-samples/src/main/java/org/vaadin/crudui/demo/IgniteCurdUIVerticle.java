package org.vaadin.crudui.demo;

import com.github.mcollovati.vertx.support.StartupContext;
import com.github.mcollovati.vertx.vaadin.*;
import com.vaadin.flow.shared.ApplicationConstants;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.vaadin.crudui.demo.authentication.AccessControl;
import org.vaadin.crudui.demo.authentication.BasicAccessControl;
import org.vaadin.crudui.demo.authentication.CurrentUser;
import org.vaadin.crudui.demo.config.VaadinConfig;

import java.util.Map;

@VaadinVerticleConfiguration(serviceName="Ignite CurdUI",mountPoint="/crud",basePackages = {"org.vaadin.hilla","org.vaadin.crudui.demo"})
public class IgniteCurdUIVerticle extends VaadinVerticle {
    private static final String JWT_COOKIE_NAME = "JWT_COOKIE";
    public static AnnotationConfigApplicationContext springContext;
    static {
        // 创建应用上下文并指定要扫描的包
        springContext = new AnnotationConfigApplicationContext();
        springContext.register(VaadinConfig.class);

        //springContext.scan("dev.hilla"); // 指定要扫描的包
        springContext.refresh();
        springContext.start();
    }
    public static JsonObject convertPropertiesToJson(Map<String, Object> properties) {

        JsonObject root = new JsonObject();

        for (String key : properties.keySet()) {
            Object value = properties.get(key);

            String[] keyParts = key.split("\\."); // 转义点号分割
            JsonObject current = root;

            // 逐层构建嵌套结构
            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                if (!current.containsKey(part)) {
                    current.put(part, new JsonObject());
                }
                current = current.getJsonObject(part);
            }

            // 设置最终值
            String lastKey = keyParts[keyParts.length - 1];
            current.put(lastKey, value);
        }

        return root;
    }

    AccessControl accessControl = new BasicAccessControl();

    public IgniteCurdUIVerticle(){

    }

    @Override
    protected ApplicationContext createSpringContext() {
        return springContext;
    }


    public JsonObject config() {
        JsonObject config = this.context.config();
        JsonObject vaadin = new JsonObject();
        vaadin.put("debug",true);
        vaadin.put("productionMode",true);
        vaadin.put("hilla.enabled",true);
        vaadin.put("devserver.enabled",false);
        vaadin.put("dev-server.port",8080);
        vaadin.put("dev-server.liveReload.enabled",false);
        vaadin.put("devmode.devTools.enabled",false);

        vaadin.put("disable-xsrf-protection",true);


        //System.setProperty("vaadin.devServerPort","8080");

        var vaadinSource = springContext.getEnvironment().getPropertySources();
        for(PropertySource propertySource: vaadinSource){
            if (propertySource.getName().contains("application.properties")) {
                System.out.println("PropertySource: " + propertySource.getName());
                // 如果是Map类型的PropertySource（如PropertiesPropertySource）
                if(propertySource instanceof CompositePropertySource){
                    propertySource = ((CompositePropertySource) propertySource).getPropertySources().iterator().next();
                    if (propertySource.getSource() instanceof Map) {
                        Map<String, Object> sourceMap = (Map<String, Object>) propertySource.getSource();
                        JsonObject json = convertPropertiesToJson(sourceMap);
                        config.mergeIn(json);

                        sourceMap.forEach((key, value) -> {
                            System.out.println(key + " = " + value);
                            this.vertx.getOrCreateContext().putLocal(key,value);
                            if(key.startsWith("vaadin.")) {
                                String name = key.replace("vaadin.", "");
                                vaadin.put(name, value);

                            }
                        });
                    }
                }
            }
        }

        Map<String, Object> sourceMap = springContext.getEnvironment().getSystemProperties();
        sourceMap.forEach((key, value) -> {
            this.vertx.getOrCreateContext().putLocal(key,value);
            if(key.startsWith("vaadin.")) {
                String name = key.replace("vaadin.", "");
                vaadin.put(name, value);
            }
        });

        config.put("vaadin",vaadin);
        for(var item: vaadin) {
            this.vertx.getOrCreateContext().putLocal(item.getKey(),item.getValue());
        }
        return config;
    }

    protected VertxVaadin createVertxVaadin(StartupContext startupContext) {
        VertxVaadin vertxVaadin = VertxVaadin.create(this.vertx, startupContext);
        Router router = vertxVaadin.router();
        router.post("/login").order(5).handler(this::handleLogin);
        router.post("/logout").order(5).handler(this::handleLogout);
        return vertxVaadin;
    }
    protected void serviceInitialized(VertxVaadinService service, Router router) {
        router.post("/login").handler(this::handleLogin);
        router.post("/logout").handler(this::handleLogout);
    }


    private void handleLogin(RoutingContext routingContext) {
        String username = routingContext.request().getFormAttribute("username");
        String password = routingContext.request().getFormAttribute("password");

        if (accessControl.isValidUser(username,password)) {
            // 登录成功
            routingContext.setUser(User.fromName(username));
            routingContext.session().put(CurrentUser.CURRENT_USER_SESSION_ATTRIBUTE_KEY, username);

            String csrfToken = java.util.UUID.randomUUID().toString();

            routingContext.response().addCookie(Cookie.cookie(ApplicationConstants.CSRF_TOKEN,csrfToken));

            // 设置响应头
            routingContext.response()
                    .putHeader("Result", "success")
                    .putHeader("Vaadin-CSRF", csrfToken)
                    .putHeader("Spring-CSRF-header", "X-CSRF-TOKEN")
                    .putHeader("Spring-CSRF-token", csrfToken)
                    //.putHeader("Saved-url", "/dashboard")
                    //.putHeader("Default-url", "/home")
                    .setStatusCode(200)
                    .end("Login successful");
        } else {
            // 登录失败
            routingContext.response()
                    .setStatusCode(401)
                    .end("Invalid credentials");
        }
    }

    private void handleLogout(RoutingContext routingContext) {
        routingContext.clearUser();
        routingContext.session().destroy();

        // 清除 JWT Cookie
        routingContext.response().removeCookie(JWT_COOKIE_NAME);

        String csrfToken = java.util.UUID.randomUUID().toString();

        routingContext.response().addCookie(Cookie.cookie(ApplicationConstants.CSRF_TOKEN,csrfToken));

        // 设置响应头
        routingContext.response()
                .putHeader("Content-Type", "text/html")
                .putHeader("Vaadin-CSRF", csrfToken)
                .putHeader("Spring-CSRF-header", "X-CSRF-TOKEN")
                .putHeader("Spring-CSRF-token", csrfToken)
                .setStatusCode(200)
                .end("<html><body><script>window.location.href='/';</script></body></html>");

    }

}
