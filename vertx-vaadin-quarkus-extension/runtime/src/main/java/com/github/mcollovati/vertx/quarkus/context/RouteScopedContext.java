/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus.context;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;

import com.github.mcollovati.vertx.quarkus.annotation.NormalRouteScoped;
import com.github.mcollovati.vertx.quarkus.annotation.NormalUIScoped;
import com.github.mcollovati.vertx.quarkus.annotation.RouteScopeOwner;
import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

import static javax.enterprise.event.Reception.IF_EXISTS;

/**
 * Context for {@link NormalRouteScoped NormalRouteScoped} beans.
 */
public class RouteScopedContext extends AbstractContext {

    public abstract static class ContextualStorageManager extends AbstractContextualStorageManager<RouteStorageKey> {

        public ContextualStorageManager() {
            // Session lock checked in VaadinSessionScopedContext while
            // getting the session attribute.
            super(false);
        }

        @Override
        protected ContextualStorage newContextualStorage(RouteStorageKey key) {
            UI.getCurrent().addDetachListener(event -> handleUIDetach(event.getUI(), key));
            return super.newContextualStorage(key);
        }

        void onAfterNavigation(@Observes(notifyObserver = IF_EXISTS) AfterNavigationEvent event) {
            Set<Class<?>> activeChain =
                    event.getActiveChain().stream().map(Object::getClass).collect(Collectors.toSet());

            destroyDescopedBeans(event.getLocationChangeEvent().getUI(), activeChain);
        }

        void onBeforeEnter(@Observes BeforeEnterEvent event) {
            UI ui = event.getUI();
            ComponentUtil.setData(
                    ui, NavigationData.class, new NavigationData(event.getNavigationTarget(), event.getLayouts()));

            Set<Class<?>> activeChain = new HashSet<>();
            activeChain.add(event.getNavigationTarget());
            activeChain.addAll(event.getLayouts());

            destroyDescopedBeans(ui, activeChain);
        }

        private void destroyDescopedBeans(UI ui, Set<Class<?>> navigationChain) {
            String uiStoreId = getUIStoreId(ui);

            Set<RouteStorageKey> missingKeys = getKeySet().stream()
                    .filter(key -> key.getUIId().equals(uiStoreId))
                    .filter(key -> !navigationChain.contains(key.getOwner()))
                    .collect(Collectors.toSet());

            missingKeys.forEach(this::destroy);
        }

        private void handleUIDetach(UI ui, RouteStorageKey key) {
            UI uiAfterRefresh = findPreservingUI(ui);
            if (uiAfterRefresh == null) {
                destroy(key);
            } else {
                uiAfterRefresh.addDetachListener(event -> handleUIDetach(event.getUI(), key));
            }
        }

        private UI findPreservingUI(UI ui) {
            VaadinSession session = ui.getSession();
            String windowName = getWindowName(ui);
            for (UI sessionUi : session.getUIs()) {
                if (sessionUi != ui && windowName != null && windowName.equals(getWindowName(sessionUi))) {
                    return sessionUi;
                }
            }
            return null;
        }

        private static String getWindowName(UI ui) {
            ExtendedClientDetails details = ui.getInternals().getExtendedClientDetails();
            if (details == null) {
                return null;
            }
            return details.getWindowName();
        }

        private RouteStorageKey getKey(UI ui, Class<?> owner) {
            ExtendedClientDetails details = ui.getInternals().getExtendedClientDetails();
            RouteStorageKey key = new RouteStorageKey(owner, getUIStoreId(ui));
            if (details == null) {
                ui.getPage().retrieveExtendedClientDetails(det -> relocate(ui, key));
            }
            return key;
        }

        private void relocate(UI ui, RouteStorageKey key) {
            relocate(key, new RouteStorageKey(key.getOwner(), getUIStoreId(ui)));
        }

        private String getUIStoreId(UI ui) {
            ExtendedClientDetails details = ui.getInternals().getExtendedClientDetails();
            if (details == null) {
                return "uid-" + ui.getUIId();
            } else {
                return "win-" + getWindowName(ui);
            }
        }

        private List<ContextualStorage> getActiveContextualStorages() {
            return getKeySet().stream()
                    .filter(key -> key.getUIId().equals(getUIStoreId(UI.getCurrent())))
                    .map(key -> getContextualStorage(key, false))
                    .collect(Collectors.toList());
        }
    }

    @VaadinSessionScoped
    @Unremovable
    static class RouteContextualStorageManager extends ContextualStorageManager {}

    static class RouteStorageKey implements Serializable {
        private final Class<?> owner;
        private final String uiId;

        private RouteStorageKey(Class<?> owner, String uiId) {
            this.owner = owner;
            this.uiId = uiId;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof RouteStorageKey)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            RouteStorageKey key = (RouteStorageKey) obj;
            return owner.equals(key.owner) && uiId.equals(key.uiId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, uiId);
        }

        @Override
        public String toString() {
            return "[ ui-key='" + getUIId() + "', owner='" + getOwner() + "' ]";
        }

        Class<?> getOwner() {
            return owner;
        }

        String getUIId() {
            return uiId;
        }
    }

    static class NavigationData {
        private final Class<?> navigationTarget;
        private final List<Class<? extends RouterLayout>> layouts;

        NavigationData(Class<?> navigationTarget, List<Class<? extends RouterLayout>> layouts) {
            this.navigationTarget = navigationTarget;
            this.layouts = layouts;
        }

        Class<?> getNavigationTarget() {
            return navigationTarget;
        }

        List<Class<? extends RouterLayout>> getLayouts() {
            return layouts;
        }
    }

    public RouteScopedContext() {
        super();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return NormalRouteScoped.class;
    }

    @Override
    public boolean isActive() {
        return Arc.container().getActiveContext(NormalUIScoped.class).isActive();
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        RouteStorageKey key = convertToKey(contextual);
        return getStorageManager().getContextualStorage(key, createIfNotExist);
    }

    @Override
    protected List<ContextualStorage> getActiveContextualStorages() {
        return getStorageManager().getActiveContextualStorages();
    }

    /**
     * Gets a bean manager.
     * <p>
     * Not a private for testing purposes only.
     *
     * @return a bean manager
     */
    BeanManager getBeanManager() {
        return Arc.container().beanManager();
    }

    /**
     * Gets a contextual storage manager class.
     * <p>
     * Not a private for testing purposes only.
     *
     * @return a contextual storage manager class
     */
    Class<? extends ContextualStorageManager> getContextualStorageManagerClass() {
        return RouteContextualStorageManager.class;
    }

    private RouteStorageKey convertToKey(Contextual<?> contextual) {
        Bean<?> bean = getBean(contextual);
        UI ui = UI.getCurrent();
        Class<?> owner = getOwner(ui, bean);
        if (!navigationChainHasOwner(ui, owner)) {
            throw new IllegalStateException(String.format(
                    "Route owner '%s' instance is not available in the "
                            + "active navigation components chain: the scope defined by the bean '%s' doesn't exist.",
                    owner, bean.getBeanClass().getName()));
        }
        return getStorageManager().getKey(ui, owner);
    }

    private boolean navigationChainHasOwner(UI ui, Class<?> owner) {
        NavigationData data = ComponentUtil.getData(ui, NavigationData.class);
        if (owner.equals(data.getNavigationTarget())) {
            return true;
        }
        return data.getLayouts().stream().anyMatch(clazz -> clazz.equals(owner));
    }

    @SuppressWarnings("unchecked")
    private Class<?> getOwner(UI ui, Bean<?> bean) {
        return bean.getQualifiers().stream()
                .filter(annotation -> annotation instanceof RouteScopeOwner)
                .map(annotation -> (Class<?>) (((RouteScopeOwner) annotation).value()))
                .findFirst()
                .orElseGet(() -> getCurrentNavigationTarget(ui, bean));
    }

    @SuppressWarnings("rawtypes")
    private Class getCurrentNavigationTarget(UI ui, Bean<?> bean) {
        NavigationData data = ComponentUtil.getData(ui, NavigationData.class);
        if (data == null) {
            throw new IllegalStateException(String.format(
                    "There is no yet any navigation chain available, "
                            + "so bean '%s' has no scope and may not be injected",
                    bean.getBeanClass().getName()));
        }
        return data.getNavigationTarget();
    }

    private ContextualStorageManager getStorageManager() {
        return BeanProvider.getContextualReference(getBeanManager(), getContextualStorageManagerClass(), false);
    }

    private Bean<?> getBean(Contextual<?> contextual) {
        if (contextual instanceof Bean) {
            return (Bean<?>) contextual;
        } else {
            throw new IllegalArgumentException(
                    contextual.getClass().getName() + " is not of type " + Bean.class.getName());
        }
    }
}
