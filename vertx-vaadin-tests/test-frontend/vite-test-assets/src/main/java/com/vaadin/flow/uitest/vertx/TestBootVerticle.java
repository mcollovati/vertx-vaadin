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
package com.vaadin.flow.uitest.vertx;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.internal.DevModeHandlerManager;
import com.vaadin.flow.server.frontend.FrontendUtils;
import com.vaadin.flow.server.frontend.TaskRunNpmInstall;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.VaadinVerticle;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;

public class TestBootVerticle extends VaadinVerticle {

    public static final Logger LOGGER = LoggerFactory.getLogger(TestBootVerticle.class);

    // Patching TaskRunNpmInstall to redirect STDERR through PIPE
    // to avoid EPIPE error -32 during npm install
    static {
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(TaskRunNpmInstall.class)
                .method(ElementMatchers.named("runNpmCommand"))
                .intercept(MethodDelegation.to(Target.class))
                .make()
                .load(TaskRunNpmInstall.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    public static final class Target {
        public static Process runNpmCommand(List<String> command, File workingDirectory) throws IOException {
            ProcessBuilder builder = FrontendUtils.createProcessBuilder(command);
            builder.environment().put("ADBLOCK", "1");
            builder.environment().put("NO_UPDATE_NOTIFIER", "1");
            builder.directory(workingDirectory);
            builder.redirectInput(ProcessBuilder.Redirect.PIPE);
            builder.redirectError(ProcessBuilder.Redirect.PIPE);

            Process process = builder.start();

            // This will allow to destroy the process which does IO regardless
            // whether it's executed in the same thread or another (may be
            // daemon) thread
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));

            return process;
        }
    }

    @Override
    protected void serviceInitialized(VertxVaadinService service, Router router) {
        String mountPoint = service.getVaadinOptions().mountPoint();
        config().getJsonArray("mountAliases", new JsonArray()).stream()
                .map(String.class::cast)
                .forEach(alias -> router.routeWithRegex(alias + "/.*").handler(ctx -> {
                    ctx.reroute(ctx.request().uri().replaceFirst(alias + "/", mountPoint + "/"));
                }));
        router.get("/__check-start").order(0).handler(ctx -> {
            LOGGER.trace("======================== check start");
            HttpServerResponse response = ctx.response();
            DevModeHandler devModeHandler =
                    DevModeHandlerManager.getDevModeHandler(service).orElse(null);
            response.setStatusCode(404);
            if (devModeHandler != null) {
                LOGGER.trace("======================== check start. dev mod 1");
                try {
                    Method isRunningMethod = findIsRunningMethod(devModeHandler.getClass());
                    boolean isRunning = (boolean) isRunningMethod.invoke(devModeHandler);
                    if (isRunning) {
                        LOGGER.info("DevModeHandler ready");
                        response.setStatusCode(200);
                    }
                } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    LOGGER.trace("======================== check start. dev mod err ");
                }
            } else if (service.getDeploymentConfiguration().isProductionMode()) {
                response.setStatusCode(200);
            }
            LOGGER.trace("======================== check start -> " + response.getStatusCode());
            response.end();
        });
    }

    private Method findIsRunningMethod(Class<?> clazz) throws NoSuchFieldException {
        do {
            try {
                Method method = clazz.getDeclaredMethod("isRunning");
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        } while (clazz != Object.class);
        throw new NoSuchFieldException("Cannot find devServerStartFuture");
    }
}
