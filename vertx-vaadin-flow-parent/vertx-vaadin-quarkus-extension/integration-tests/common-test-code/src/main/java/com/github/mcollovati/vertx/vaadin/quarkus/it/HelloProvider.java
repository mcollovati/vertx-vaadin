package com.github.mcollovati.vertx.vaadin.quarkus.it;

import java.io.Serializable;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

@VaadinSessionScoped
public class HelloProvider implements Serializable {
    private String hello;

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
