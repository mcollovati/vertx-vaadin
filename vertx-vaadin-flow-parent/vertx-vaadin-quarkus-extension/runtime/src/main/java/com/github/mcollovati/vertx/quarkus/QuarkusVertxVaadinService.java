package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Optional;
import java.util.Set;

import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinService;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
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

public class QuarkusVertxVaadinService extends VertxVaadinService {

    private final BeanManager beanManager;
    private final UIEventListener uiEventListener;

    public QuarkusVertxVaadinService(VertxVaadin vertxVaadin, DeploymentConfiguration deploymentConfiguration, BeanManager beanManager) {
        super(vertxVaadin, deploymentConfiguration);
        this.beanManager = beanManager;
        uiEventListener = new UIEventListener(beanManager);
    }

    @Override
    public void init() throws ServiceException {
        addEventListeners();
        lookup(SystemMessagesProvider.class)
            .ifPresent(this::setSystemMessagesProvider);
        super.init();
    }

    @Override
    public void fireUIInitListeners(UI ui) {
        addUIListeners(ui);
        super.fireUIInitListeners(ui);
    }

    @Override
    public Optional<Instantiator> loadInstantiators() throws ServiceException {
        final Set<Bean<?>> beans = beanManager.getBeans(Instantiator.class,
            BeanLookup.SERVICE);
        if (beans == null || beans.isEmpty()) {
            throw new ServiceException("Cannot init VaadinService "
                + "because no CDI instantiator bean found.");
        }
        final Bean<Instantiator> bean;
        try {
            // noinspection unchecked
            bean = (Bean<Instantiator>) beanManager.resolve(beans);
        } catch (final AmbiguousResolutionException e) {
            throw new ServiceException(
                "There are multiple eligible CDI "
                    + Instantiator.class.getSimpleName() + " beans.",
                e);
        }

        // Return the contextual instance (rather than CDI proxy) as it will be
        // stored inside VaadinService. Not relying on the proxy allows
        // accessing VaadinService::getInstantiator even when
        // VaadinServiceScopedContext is not active
        final CreationalContext<Instantiator> creationalContext = beanManager
            .createCreationalContext(bean);
        final Context context = beanManager.getContext(ApplicationScoped.class); // VaadinServiceScoped
        final Instantiator instantiator = context.get(bean, creationalContext);

        if (!instantiator.init(this)) {
            throw new ServiceException("Cannot init VaadinService because "
                + instantiator.getClass().getName() + " CDI bean init()"
                + " returned false.");
        }
        return Optional.of(instantiator);
    }

    private void addEventListeners() {
        addServiceDestroyListener(this::fireCdiDestroyEvent);
        addUIInitListener(getBeanManager()::fireEvent);
        addSessionInitListener(this::sessionInit);
        addSessionDestroyListener(this::sessionDestroy);
    }

    private void sessionInit(SessionInitEvent sessionInitEvent)
        throws ServiceException {
        VaadinSession session = sessionInitEvent.getSession();
        lookup(ErrorHandler.class).ifPresent(session::setErrorHandler);
        getBeanManager().fireEvent(sessionInitEvent);
    }

    private void sessionDestroy(SessionDestroyEvent sessionDestroyEvent) {
        getBeanManager().fireEvent(sessionDestroyEvent);
    }

    private void fireCdiDestroyEvent(ServiceDestroyEvent event) {
        try {
            beanManager.fireEvent(event);
        } catch (Exception e) {
            // During application shutdown on TomEE 7,
            // beans are lost at this point.
            // Does not throw an exception, but catch anything just to be sure.
            getLogger().warn("Error at destroy event distribution with CDI.",
                e);
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
            T instance = new BeanLookup<>(getBeanManager(), type,
                BeanLookup.SERVICE).lookup();
            return Optional.ofNullable(instance);
        } catch (AmbiguousResolutionException e) {
            throw new ServiceException("There are multiple eligible CDI "
                + type.getSimpleName() + " beans.", e);
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
        implements AfterNavigationListener, BeforeEnterListener,
        BeforeLeaveListener, ComponentEventListener<PollEvent> {

        private BeanManager beanManager;

        UIEventListener(BeanManager beanManager) {
            this.beanManager = beanManager;
        }

        @Override
        public void afterNavigation(AfterNavigationEvent event) {
            getBeanManager().fireEvent(event);
        }

        @Override
        public void beforeEnter(BeforeEnterEvent event) {
            getBeanManager().fireEvent(event);
        }

        @Override
        public void beforeLeave(BeforeLeaveEvent event) {
            getBeanManager().fireEvent(event);
        }

        @Override
        public void onComponentEvent(PollEvent event) {
            getBeanManager().fireEvent(event);
        }

        private BeanManager getBeanManager() {
            return beanManager;
        }
    }

}
