package com.github.mcollovati.vertx.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.di.DefaultInstantiator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.internal.UsageStatistics;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.auth.MenuAccessControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.util.ClassUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SpringInstantiator extends DefaultInstantiator {
    static Logger log = LoggerFactory.getLogger(SpringInstantiator.class.getName());

    private ApplicationContext context;
    private AtomicBoolean loggingEnabled = new AtomicBoolean(true);

    public SpringInstantiator(VaadinService service, ApplicationContext context) {
        super(service);
        this.context = context;
        UsageStatistics.markAsUsed("flow/SpringInstantiator", (String)null);
        UsageStatistics.markAsUsed("SpringFramework", (String)Optional.ofNullable(SpringVersion.getVersion()).orElse("unknown"));


    }

    public Stream<VaadinServiceInitListener> getServiceInitListeners() {
        Stream<VaadinServiceInitListener> springListeners = Stream.concat(Stream.of((event) -> {
            this.context.publishEvent(event);
        }), this.context.getBeansOfType(VaadinServiceInitListener.class).values().stream());

        Stream<VaadinServiceInitListener> sysListeners = super.getServiceInitListeners();

        return Stream.concat(sysListeners, springListeners);
    }

    public <T extends Component> T createComponent(Class<T> componentClass) {
        return (T)this.context.getAutowireCapableBeanFactory().createBean(componentClass);
    }

    public I18NProvider getI18NProvider() {
        int beansCount = this.context.getBeanNamesForType(I18NProvider.class).length;
        if (beansCount == 1) {
            return this.context.getBean(I18NProvider.class);
        } else {
            if (this.loggingEnabled.compareAndSet(true, false)) {
                log.info("The number of beans implementing '{}' is {}. Cannot use Spring beans for I18N, falling back to the default behavior", I18NProvider.class.getSimpleName(), beansCount);
            }

            return super.getI18NProvider();
        }
    }

    public MenuAccessControl getMenuAccessControl() {
        int beansCount = this.context.getBeanNamesForType(MenuAccessControl.class).length;
        if (beansCount == 1) {
            return (MenuAccessControl)this.context.getBean(MenuAccessControl.class);
        } else {
            if (this.loggingEnabled.compareAndSet(true, false)) {
                log.info("The number of beans implementing '{}' is {}. Cannot use Spring beans for Menu Access Control, falling back to the default behavior", MenuAccessControl.class.getSimpleName(), beansCount);
            }

            return super.getMenuAccessControl();
        }
    }

    public <T> T getOrCreate(Class<T> type) {
        if (this.context.getBeanNamesForType(type).length == 1) {
            return this.context.getBean(type);
        } else if (this.context.getBeanNamesForType(type).length > 1) {
            try {
                return this.context.getAutowireCapableBeanFactory().createBean(type);
            } catch (BeanInstantiationException var3) {
                throw new BeanInstantiationException(var3.getBeanClass(), "[HINT] This could be caused by more than one suitable beans for autowiring in the context.", var3);
            }
        } else {
            return this.context.getAutowireCapableBeanFactory().createBean(type);
        }
    }

    public Class<?> getApplicationClass(Class<?> clazz) {
        return ClassUtils.getUserClass(clazz);
    }
}
