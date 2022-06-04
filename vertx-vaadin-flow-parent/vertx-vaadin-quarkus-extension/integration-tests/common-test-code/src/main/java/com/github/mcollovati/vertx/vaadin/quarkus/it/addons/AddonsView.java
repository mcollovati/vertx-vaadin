package com.github.mcollovati.vertx.vaadin.quarkus.it.addons;

import javax.annotation.PostConstruct;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import org.vaadin.jandex.HelloWorldJandex;
import org.vaadin.nojandex.HelloWorld;

@Route("addons")
public class AddonsView extends Div {

    @PostConstruct
    private void init() {
        add(new HelloWorld(), new HelloWorldJandex());
    }
}
