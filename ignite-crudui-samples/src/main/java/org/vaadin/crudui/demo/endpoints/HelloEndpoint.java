package org.vaadin.crudui.demo.endpoints;

import com.vaadin.hilla.Endpoint;
import com.vaadin.hilla.Nonnull;
import jakarta.annotation.security.PermitAll;
import org.vaadin.crudui.demo.service.GreetService;


@Endpoint
@PermitAll
public class HelloEndpoint {

    GreetService service = new GreetService();

    @Nonnull
    public String sayHello(@Nonnull String name) {

        return service.greet(name);
    }
}
