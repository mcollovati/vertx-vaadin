package org.vaadin.crudui.demo.authentication;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.Endpoint;

import java.util.Date;

@Endpoint
@AnonymousAllowed
public class JwtAuthEndpoint {
    private final String SECRET_KEY = "your-secret-key";

    AccessControl accessControl = new BasicAccessControl();

    public String authenticate(String username, String password) {
        if (accessControl.isValidUser(username, password)) {
            accessControl.signIn(username,password);
            String token = username;
            return token;
        }
        throw new RuntimeException("认证失败");
    }

}