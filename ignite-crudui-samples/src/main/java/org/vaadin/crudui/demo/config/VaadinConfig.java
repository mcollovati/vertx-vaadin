package org.vaadin.crudui.demo.config;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan({"org.vaadin.crudui.ignite","org.vaadin.crudui.demo"})
@Import(JpaConfig.class)
@Theme(value = "hillagroceryapp")
public class VaadinConfig  implements AppShellConfigurator {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    public void configurePage(AppShellSettings settings) {
        String uri = settings.getRequest().getContextPath();
        System.out.println(uri);

    }
}
