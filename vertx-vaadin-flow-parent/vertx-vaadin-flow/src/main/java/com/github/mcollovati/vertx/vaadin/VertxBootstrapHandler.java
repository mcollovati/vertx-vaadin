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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.NavigationState;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import io.vertx.core.VertxException;

/**
 * Replacement of Vaadin BootstrapHandler to get rid of some explicit casts
 * in BootstrapUtils.
 */
public class VertxBootstrapHandler extends BootstrapHandler {

    private static final boolean APPLY_PATCH = VertxBC.Delegator.shouldPatch();

    /**
     * Some versions of flow uses a {@link BootstrapHandler} implementation that has explicit casts
     * on VaadinServletRequest. We try to detect if there's the need for a patched {@link BootstrapHandler}
     * or if we can use the original one.
     *
     * @return
     */
    public static BootstrapHandler patchIfNeeded() {
        if (APPLY_PATCH) {
            return new VertxBootstrapHandler();
        }
        return new BootstrapHandler();
    }


    @Override
    protected BootstrapContext createBootstrapContext(VaadinRequest request, VaadinResponse response, UI ui, Function<VaadinRequest, String> contextPathCallback) {
        return new VertxBC(request, response, ui.getInternals().getSession(), ui, contextPathCallback);
    }

    private static class VertxBC extends BootstrapContext {

        protected VertxBC(VaadinRequest request, VaadinResponse response, VaadinSession session, UI ui,
                          Function<VaadinRequest, String> contextCallback) {
            super(request, response, session, new FakeUI(), contextCallback);
            setField("ui", ui);
            setField("pageConfigurationHolder", Delegator.resolvePageConfigurationHolder(ui, request).orElse(null));
        }

        private void setField(String name, Object value) {
            try {
                Field field = BootstrapContext.class.getDeclaredField(name);
                field.setAccessible(true);
                field.set(this, value);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        static class FakeUI extends UI {
            @Override
            public Router getRouter() {
                return null;
            }
        }

        private static class Delegator {

            @SuppressWarnings("unchecked")
            private static <T> T invoke(String name, Class<?>[] paramTypes, Object... args) {
                return findMethod(name, paramTypes).map(method -> {
                    try {
                        return (T) method.invoke(null, args);
                    } catch (Exception e) {
                        throw new VertxException(e);
                    }
                }).orElseThrow(() -> new VertxException("Cannot invoke BootstrapUtils." + name));
            }

            private static Optional<Method> findMethod(String name, Class<?>[] paramTypes) {
                try {
                    Class<?> bootstrapUtilsClass = Class.forName("com.vaadin.flow.server.BootstrapUtils");
                    Method method = bootstrapUtilsClass.getDeclaredMethod(name, paramTypes);
                    method.setAccessible(true);
                    return Optional.of(method);
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    return Optional.empty();
                }
            }

            private static boolean shouldPatch() {
                return findMethod("resolveRouteNotFoundNavigationTarget", new Class[]{ServletContext.class}).isPresent();
            }

            public static Optional<Class<?>> resolvePageConfigurationHolder(UI ui, VaadinRequest request) {
                assert ui != null;
                assert request != null;
                if (ui.getRouter() == null) {
                    return Optional.empty();
                }
                Optional<Class<?>> navigationTarget = ui.getRouter()
                    .resolveNavigationTarget(request.getPathInfo(),
                        request.getParameterMap())
                    .map(navigationState -> resolveTopParentLayout(ui, navigationState));
                if (navigationTarget.isPresent()) {
                    return navigationTarget;
                }
                // If there is no route target available then let's ask for "route not
                // found" target
                return resolveRouteNotFoundNavigationTarget(
                    ((VertxVaadinService) ui.getSession().getService())
                        .getServletContext())
                    .map(errorNavigationTarget -> {
                        /*
                         * {@code resolveTopParentLayout} is theoretically the
                         * correct way to get the parent layout. But in fact it does
                         * work for non route targets.
                         */
                        List<Class<? extends RouterLayout>> layouts = RouteUtil
                            .getParentLayoutsForNonRouteTarget(
                                errorNavigationTarget);
                        if (layouts.isEmpty()) {
                            return errorNavigationTarget;
                        } else {
                            return layouts.get(layouts.size() - 1);
                        }
                    });
            }

            private static Optional<Class<? extends Component>> resolveRouteNotFoundNavigationTarget(ServletContext context) {
                return invoke("resolveRouteNotFoundNavigationTarget", new Class[]{ServletContext.class}, context);
            }

            private static Class<?> resolveTopParentLayout(UI ui, NavigationState navigationState) {
                return invoke("resolveTopParentLayout", new Class[]{UI.class, NavigationState.class}, ui, navigationState);
            }

        }


    }
}

