package com.github.mcollovati.vertx.vaadin.quarkus.it;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;

@Theme("reusable-theme")
@Push
public class AppShellConfig implements AppShellConfigurator {
}
