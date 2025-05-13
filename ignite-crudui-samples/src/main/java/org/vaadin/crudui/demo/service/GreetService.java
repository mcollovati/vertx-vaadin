package org.vaadin.crudui.demo.service;

import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class GreetService implements Serializable {

    public String greet(String name) {
        if (name == null || name.isEmpty()) {
            return "Hello anonymous user";
        } else {
            return "Hello " + name;
        }
    }

}
