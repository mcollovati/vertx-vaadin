package com.vaadin.flow;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.VaadinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAppShell implements AppShellConfigurator {

    private static Logger logger = LoggerFactory.getLogger(TestAppShell.class);

    @Override
    public void configurePage(AppShellSettings settings) {

        VaadinService service = settings.getRequest().getService();
        service.getRouter().getRegistry()
            .getNavigationTarget(settings.getRequest().getPathInfo())
            .flatMap(this::findTestPushAnnotation)
            .ifPresent(push -> {
                logger.debug("Setting push {}", push.value());
                service.addUIInitListener(event -> {
                    event.getUI().getPushConfiguration().setPushMode(push.value());
                    event.getUI().getPushConfiguration().setTransport(push.transport());
                });
            });
    }

    private Optional<TestPush> findTestPushAnnotation(Class<? extends Component> target) {
        TestPush ann = target.getAnnotation(TestPush.class);
        ParentLayout parentLayout = target.getAnnotation(ParentLayout.class);
        if (ann == null && parentLayout != null) {
            ann = parentLayout.value().getAnnotation(TestPush.class);
        }
        return Optional.ofNullable(ann);
    }
}
