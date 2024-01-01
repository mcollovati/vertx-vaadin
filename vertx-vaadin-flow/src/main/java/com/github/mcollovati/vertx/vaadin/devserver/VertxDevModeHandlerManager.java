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
package com.github.mcollovati.vertx.vaadin.devserver;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import com.vaadin.base.devserver.AbstractDevServerRunner;
import com.vaadin.base.devserver.ViteHandler;
import com.vaadin.flow.internal.DevModeHandler;
import com.vaadin.flow.server.frontend.FrontendUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.LoggerFactory;

import static com.vaadin.flow.server.Constants.VAADIN_MAPPING;

public class VertxDevModeHandlerManager /* extends DevModeHandlerManagerImpl */ {

    // Patching TaskRunNpmInstall to redirect STDERR through PIPE
    // to avoid EPIPE error -32 during npm install
    public static void patchViteHandler() {
        try {
            Class.forName(
                    "com.vaadin.base.devserver.ViteHandler", false, VertxDevModeHandlerManager.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(VertxDevModeHandlerManager.class).debug("ViteHandler not present");
            return;
        }
        ByteBuddyAgent.install();
        new ByteBuddy()
                .redefine(ViteHandler.class)
                .method(ElementMatchers.named("getPathToVaadin"))
                .intercept( // FixedValue.value("/VAADIN/"))
                        MethodDelegation.to(Target.class))
                .make()
                .load(ViteHandler.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    public static final class Target {
        public static String getPathToVaadin() {
            String path = FrontendUtils.getFrontendServletPath(null);
            return path.replaceFirst("/$", "") + "/" + VAADIN_MAPPING;
        }
    }

    private static AbstractDevServerRunner unwrapHandler(DevModeHandler devModeHandler) {
        if (devModeHandler instanceof AbstractDevServerRunner) {
            return (AbstractDevServerRunner) devModeHandler;
        } // else if (devModeHandler instanceof ViteHandlerWrapper) {
        // return ((ViteHandlerWrapper) devModeHandler).viteHandler;
        // }
        throw new IllegalArgumentException("Given DevModeHandler does not extend "
                + AbstractDevServerRunner.class.getName() + ": "
                + devModeHandler.getClass().getName());
    }

    public static CompletableFuture<Integer> getDevModeHandlerFuture(DevModeHandler devModeHandler) {
        try {
            Field devServerStartFuture = AbstractDevServerRunner.class.getDeclaredField("devServerStartFuture");
            devServerStartFuture.setAccessible(true);
            return ((CompletableFuture<Void>) devServerStartFuture.get(unwrapHandler(devModeHandler)))
                    .thenApply(unused -> devModeHandler.getPort());
        } catch (Exception ex) {
            LoggerFactory.getLogger(VertxDevModeHandlerManager.class).error("Cannot get DevModHandler future", ex);
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }
    /*
     *
     * private DevModeHandler wrapViteHandler(DevModeHandler devModeHandler) {
     * if (devModeHandler instanceof ViteHandler) { return new
     * ViteHandlerWrapper((ViteHandler) devModeHandler); } return
     * devModeHandler; }
     *
     * @Override public void setDevModeHandler(DevModeHandler devModeHandler) {
     * super.setDevModeHandler(wrapViteHandler(devModeHandler)); }
     *
     *
     * private static class ViteHandlerWrapper implements DevModeHandler {
     *
     * private static final String DEV_SERVER_HOST = "http://127.0.0.1"; private
     * static final int DEFAULT_TIMEOUT = 120 * 1000; private final ViteHandler
     * viteHandler;
     *
     * public ViteHandlerWrapper(ViteHandler viteHandler) { this.viteHandler =
     * viteHandler; }
     *
     * @Override public String getFailedOutput() { return
     * viteHandler.getFailedOutput(); }
     *
     * @Override public HttpURLConnection prepareConnection(String path, String
     * method) throws IOException { // path should have been checked at this
     * point for any outside requests URL uri = new URL(DEV_SERVER_HOST + ":" +
     * getPort() + path); HttpURLConnection connection = (HttpURLConnection)
     * uri.openConnection(); connection.setRequestMethod(method);
     * connection.setReadTimeout(DEFAULT_TIMEOUT);
     * connection.setConnectTimeout(DEFAULT_TIMEOUT); return connection; }
     *
     * @Override public boolean serveDevModeRequest(HttpServletRequest request,
     * HttpServletResponse response) throws IOException { return
     * viteHandler.serveDevModeRequest(request, response); }
     *
     * @Override public void stop() { viteHandler.stop(); }
     *
     * @Override public File getProjectRoot() { return
     * viteHandler.getProjectRoot(); }
     *
     * @Override public int getPort() { return viteHandler.getPort(); }
     *
     * @Override public boolean handleRequest(VaadinSession session,
     * VaadinRequest request, VaadinResponse response) throws IOException {
     * return viteHandler.handleRequest(session, request, response); } }
     *
     */
}
