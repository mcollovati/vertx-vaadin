/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.mcollovati.vertx.vaadin.connect.auth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.connect.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VaadinConnectAccessCheckerSupport<REQUEST> implements VaadinConnectAccessChecker<REQUEST> {

    protected boolean xsrfProtectionEnabled = true;

    /**
     * Check that the endpoint is accessible for the current user.
     *
     * @param method  the Vaadin endpoint method to check ACL
     * @param request the request that triggers the <code>method</code> invocation
     * @return an error String with an issue description, if any validation
     * issues occur, {@code null} otherwise
     */
    public String check(Method method, REQUEST request) {
        if (isAnonymous(request)) {
            return verifyAnonymousUser(method, request);
        } else {
            return verifyAuthenticatedUser(method, request);
        }
    }

    protected abstract boolean isAnonymous(REQUEST request);

    protected abstract boolean hasSession(REQUEST request);

    protected abstract boolean isUserInRole(REQUEST request, String role);

    protected abstract String getHeader(REQUEST request, String headerName);

    protected abstract Object getSessionAttribute(REQUEST request, String attributeName);

    /**
     * Gets the entity to check for Vaadin Connect security restrictions.
     *
     * @param method the method to analyze, not {@code null}
     * @return the entity that is responsible for security settings for the
     * method passed
     * @throws IllegalArgumentException if the method is not public
     */
    public AnnotatedElement getSecurityTarget(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException(String.format(
                "The method '%s' is not public hence cannot have a security target",
                method));
        }
        return hasSecurityAnnotation(method) ? method
            : method.getDeclaringClass();
    }

    private String verifyAnonymousUser(Method method, REQUEST request) {
        if (!getSecurityTarget(method)
            .isAnnotationPresent(AnonymousAllowed.class)
            || cannotAccessMethod(method, request)) {
            return "Anonymous access is not allowed";
        }
        return null;
    }

    private String verifyAuthenticatedUser(Method method, REQUEST request) {
        if (cannotAccessMethod(method, request)) {
            return "Unauthorized access to Vaadin endpoint";
        }
        return null;
    }

    private boolean cannotAccessMethod(Method method, REQUEST request) {
        return requestForbidden(request) || entityForbidden(getSecurityTarget(method), request);
    }

    private boolean requestForbidden(REQUEST request) {
        if (!xsrfProtectionEnabled) {
            return false;
        }

        if (!hasSession(request)) {
            return false;
        }

        String csrfTokenInSession = (String) getSessionAttribute(request, VaadinService.getCsrfTokenAttributeName());
        if (csrfTokenInSession == null) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(
                    "Unable to verify CSRF token for endpoint request, got null token in session");
            }

            return true;
        }

        if (!csrfTokenInSession.equals(getHeader(request, "X-CSRF-Token"))) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Invalid CSRF token in endpoint request");
            }

            return true;
        }

        return false;
    }

    private boolean entityForbidden(AnnotatedElement entity, REQUEST request) {
        return entity.isAnnotationPresent(DenyAll.class) || (!entity
            .isAnnotationPresent(AnonymousAllowed.class)
            && !roleAllowed(entity.getAnnotation(RolesAllowed.class),
            request));
    }

    private boolean roleAllowed(RolesAllowed rolesAllowed, REQUEST request) {
        if (rolesAllowed == null) {
            return true;
        }

        for (String role : rolesAllowed.value()) {
            if (isUserInRole(request, role)) {
                return true;
            }
        }

        return false;
    }


    private boolean hasSecurityAnnotation(Method method) {
        return method.isAnnotationPresent(AnonymousAllowed.class)
            || method.isAnnotationPresent(PermitAll.class)
            || method.isAnnotationPresent(DenyAll.class)
            || method.isAnnotationPresent(RolesAllowed.class);
    }

    /**
     * Enable or disable XSRF token checking in endpoints.
     *
     * @param xsrfProtectionEnabled enable or disable protection.
     */
    public void enableCsrf(boolean xsrfProtectionEnabled) {
        this.xsrfProtectionEnabled = xsrfProtectionEnabled;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinConnectAccessChecker.class);
    }

}
