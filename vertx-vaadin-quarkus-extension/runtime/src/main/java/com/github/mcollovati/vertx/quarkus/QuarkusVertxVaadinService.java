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
package com.github.mcollovati.vertx.quarkus;

import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.AmbiguousResolutionException;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import com.vaadin.flow.di.InstantiatorFactory;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveListener;
import com.vaadin.flow.router.ListenerPriority;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.ServiceDestroyEvent;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.SystemMessagesProvider;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;

public class QuarkusVertxVaadinService extends VertxVaadinService {

    private final BeanManager beanManager;
    private final UIEventListener uiEventListener;

    public QuarkusVertxVaadinService(
            VertxVaadin vertxVaadin, DeploymentConfiguration deploymentConfiguration, BeanManager beanManager) {
        super(vertxVaadin, deploymentConfiguration);
        this.beanManager = beanManager;
        uiEventListener = new UIEventListener(beanManager);
    }

    @Override
    public void init() throws ServiceException {
        addEventListeners();
        lookup(SystemMessagesProvider.class).ifPresent(this::setSystemMessagesProvider);
        super.init();
    }

    @Override
    public void fireUIInitListeners(UI ui) {
        addUIListeners(ui);
        super.fireUIInitListeners(ui);
    }

    @Override
    public Optional<Instantiator> loadInstantiators() throws ServiceException {
        final Set<Bean<?>> beans = beanManager.getBeans(InstantiatorFactory.class,
                BeanLookup.SERVICE);
        if (beans == null || beans.isEmpty()) {
            throw new ServiceException("Cannot init VaadinService "
                    + "because no CDI instantiator factory bean found.");
        }
        final Bean<InstantiatorFactory> bean;
        try {
            // noinspection unchecked
            bean = (Bean<InstantiatorFactory>) beanManager.resolve(beans);
        } catch (final AmbiguousResolutionException e) {
            throw new ServiceException(
                    "There are multiple eligible CDI "
                            + InstantiatorFactory.class.getSimpleName() + " beans.",
                    e);
        }

        // Return the contextual instance (rather than CDI proxy) as it will be
        // stored inside VaadinService. Not relying on the proxy allows
        // accessing VaadinService::getInstantiator even when
        // VaadinServiceScopedContext is not active
        final CreationalContext<InstantiatorFactory> creationalContext = beanManager
                .createCreationalContext(bean);
        final Context context = beanManager.getContext(ApplicationScoped.class); // VaadinServiceScoped
        final InstantiatorFactory instantiatorFactory = context.get(bean,
                creationalContext);

        Instantiator instantiator = instantiatorFactory.createInstantitor(this);
        if (instantiator == null) {
            throw new ServiceException("Cannot init VaadinService because "
                    + Instantiator.class.getSimpleName() + " is null");
        }
        return Optional.of(instantiator);
    }

    private void addEventListeners() {
        addServiceDestroyListener(this::fireCdiDestroyEvent);
        addUIInitListener(getBeanManager().getEvent()::fire);
        addSessionInitListener(this::sessionInit);
        addSessionDestroyListener(this::sessionDestroy);
    }

    private void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
        VaadinSession session = sessionInitEvent.getSession();
        lookup(ErrorHandler.class).ifPresent(session::setErrorHandler);
        getBeanManager().getEvent().fire(sessionInitEvent);
    }

    private void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        getBeanManager().getEvent().fire(sessionDestroyEvent);
    }

    private void fireCdiDestroyEvent(ServiceDestroyEvent event) {
        try {
            beanManager.getEvent().fire(event);
        } catch (Exception e) {
            // During application shutdown on TomEE 7,
            // beans are lost at this point.
            // Does not throw an exception, but catch anything just to be sure.
            getLogger().warn("Error at destroy event distribution with CDI.", e);
        }
    }

    private void addUIListeners(UI ui) {
        ui.addAfterNavigationListener(uiEventListener);
        ui.addBeforeLeaveListener(uiEventListener);
        ui.addBeforeEnterListener(uiEventListener);
        ui.addPollListener(uiEventListener);
    }

    /**
     * Gets an instance of a {@code @VaadinServiceEnabled} annotated bean of the
     * given {@code type}.
     *
     * @param type the required service type
     * @param <T>  the type of the service
     * @return an {@link Optional} wrapping the service instance, or
     * {@link Optional#empty()} if no bean definition exists for given
     * type.
     * @throws ServiceException if multiple beans exists for the given type.
     */
    public <T> Optional<T> lookup(Class<T> type) throws ServiceException {
        try {
            T instance = new BeanLookup<>(getBeanManager(), type, BeanLookup.SERVICE).lookup();
            return Optional.ofNullable(instance);
        } catch (AmbiguousResolutionException e) {
            throw new ServiceException("There are multiple eligible CDI " + type.getSimpleName() + " beans.", e);
        }
    }

    private BeanManager getBeanManager() {
        return beanManager;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(QuarkusVertxVaadinService.class);
    }

    /**
     * Static listener class, to avoid registering the whole service instance.
     */
    @ListenerPriority(-100) // navigation event listeners are last by default
    private static class UIEventListener
            implements AfterNavigationListener,
                    BeforeEnterListener,
                    BeforeLeaveListener,
                    ComponentEventListener<PollEvent> {

        private BeanManager beanManager;

        UIEventListener(BeanManager beanManager) {
            this.beanManager = beanManager;
        }

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
            getBeanManager().getEvent().fire(event);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
            getBeanManager().getEvent().fire(event);
        }

        @Override
        public void beforeLeave(BeforeLeaveEvent event) {
            getBeanManager().getEvent().fire(event);
        }

        @Override
        public void onComponentEvent(PollEvent event) {
            getBeanManager().getEvent().fire(event);
        }

        private BeanManager getBeanManager() {
            return beanManager;
        }
    }
}
