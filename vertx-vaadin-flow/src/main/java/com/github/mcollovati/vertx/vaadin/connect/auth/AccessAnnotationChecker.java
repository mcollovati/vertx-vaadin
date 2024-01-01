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

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.function.Function;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Checks if a given user has access to a given method.
 * <p>
 * Check is performed as follows when called for a method:
 * <ol>
 * <li>A security annotation (see below) is searched for on that particular
 * method.</li>
 * <li>If a security annotation was not found on the method, checks the class
 * the method is declared in.</li>
 * <li>If no security annotation was found, deny access by default</li>
 * </ol>
 * <p>
 * The security annotations checked and their meaning are:
 * <ul>
 * <li>{@link AnonymousAllowed} - allows access to any logged on or not logged
 * in user. Public access.</li>
 * <li>{@link PermitAll} - allows access to any logged in user but denies access
 * to anonymous users.</li>
 * <li>{@link RolesAllowed} - allows access there is a logged in user that has
 * any of the roles mentioned in the annotation</li>
 * <li>{@link DenyAll} - denies access.</li>
 * </ul>
 *
 * <p>
 * Source adapted from Vaadin Flow (https://github.com/vaadin/flow) fusion-endpoints module,
 * to get rid of servlet APIs.
 * <p>
 *  @param <REQUEST> request type
 */
public abstract class AccessAnnotationChecker<REQUEST> implements Serializable {

    /**
     * Checks if the user defined by the current active servlet request (using
     * {@link VaadinRequest#getUserPrincipal()} and
     * {@link VaadinRequest#isUserInRole(String)} has access to the given
     * method.
     *
     * @param method the method to check access to
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Method method) {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request == null) {
            throw new IllegalStateException(
                    "No request is available. This method can only be used with an active VaadinRequest");
        }
        return hasAccess(method, request);
    }

    /**
     * Checks if the user defined by the current active servlet request (using
     * {@link VaadinRequest#getUserPrincipal()} and
     * {@link VaadinRequest#isUserInRole(String)} has access to the given
     * class.
     *
     * @param cls the class to check access to
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Class<?> cls) {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request == null) {
            throw new IllegalStateException(
                    "No request is available. This method can only be used with an active VaadinServletRequest");
        }
        return hasAccess(cls, request);
    }

    /**
     * Checks if the user defined by the request (using
     * {@link VaadinRequest#getUserPrincipal()} and
     * {@link VaadinRequest#isUserInRole(String)} has access to the given
     * method.
     *
     * @param method  the method to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Method method, UserAwareRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(method, request.getUserPrincipal(), request::isUserInRole);
    }

    /**
     * Checks if the user defined by the request (using
     * {@link UserAwareRequest#getUserPrincipal()} and
     * {@link UserAwareRequest#isUserInRole(String)} has access to the given
     * class.
     *
     * @param cls     the class to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Class<?> cls, UserAwareRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(cls, request.getUserPrincipal(), request::isUserInRole);
    }

    /**
     * Checks if the user defined by the request (adapting request to {@link UserAwareRequest}
     * and using {@link UserAwareRequest#getUserPrincipal()} and
     * {@link UserAwareRequest#isUserInRole(String)} has access to the given
     * method.
     *
     * @param method  the method to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Method method, REQUEST request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(method, adaptRequest(request));
    }

    /**
     * Checks if the user defined by the request (adapting request to {@link UserAwareRequest}
     * and using {@link HttpServletRequest#getUserPrincipal()} and
     * {@link HttpServletRequest#isUserInRole(String)} has access to the given
     * class.
     *
     * @param cls     the class to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Class<?> cls, REQUEST request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(cls, adaptRequest(request));
    }

    /**
     * Checks if the user defined by the request (using
     * {@link VaadinRequest#getUserPrincipal()} and
     * {@link VaadinRequest#isUserInRole(String)} has access to the given
     * method.
     *
     * @param method  the method to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Method method, VaadinRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(method, request.getUserPrincipal(), request::isUserInRole);
    }

    /**
     * Checks if the user defined by the request (using
     * {@link VaadinRequest#getUserPrincipal()} and
     * {@link VaadinRequest#isUserInRole(String)} has access to the given
     * class.
     *
     * @param cls     the class to check access to
     * @param request the http request to use for user information
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Class<?> cls, VaadinRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null");
        }
        return hasAccess(cls, request.getUserPrincipal(), request::isUserInRole);
    }

    protected abstract UserAwareRequest adaptRequest(REQUEST request);

    /**
     * Checks if the user defined by the given {@link Principal} and role
     * checker has access to the given method.
     *
     * @param method      the method to check access to
     * @param principal   the principal of the user
     * @param roleChecker a function that can answer if a user has a given role
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Method method, Principal principal, Function<String, Boolean> roleChecker) {
        return hasAccess(getSecurityTarget(method), principal, roleChecker);
    }

    /**
     * Checks if the user defined by the given {@link Principal} and role
     * checker has access to the given class.
     *
     * @param cls         the class to check access to
     * @param principal   the principal of the user
     * @param roleChecker a function that can answer if a user has a given role
     * @return {@code true} if the user has access to the given method,
     * {@code false} otherwise
     */
    public boolean hasAccess(Class<?> cls, Principal principal, Function<String, Boolean> roleChecker) {
        return hasAccess(getSecurityTarget(cls), principal, roleChecker);
    }

    /**
     * Gets the method or class to check for security restrictions.
     *
     * @param method the method to look up
     * @return the entity that is responsible for security settings for the
     * method passed
     * @throws IllegalArgumentException if the method is not public
     */
    public AnnotatedElement getSecurityTarget(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException(
                    String.format("The method '%s' is not public hence cannot have a security target", method));
        }
        return hasSecurityAnnotation(method) ? method : method.getDeclaringClass();
    }

    /**
     * Gets the class to check for security restrictions.
     *
     * @param cls the class to check
     * @return the entity that is responsible for security settings for the
     * method passed
     * @throws IllegalArgumentException if the method is not public
     */
    public AnnotatedElement getSecurityTarget(Class<?> cls) {
        return cls;
    }

    private boolean hasAccess(
            AnnotatedElement annotatedClassOrMethod, Principal principal, Function<String, Boolean> roleChecker) {
        if (annotatedClassOrMethod.isAnnotationPresent(DenyAll.class)) {
            return false;
        }
        if (annotatedClassOrMethod.isAnnotationPresent(AnonymousAllowed.class)) {
            return true;
        }
        if (principal == null) {
            return false;
        }
        RolesAllowed rolesAllowed = annotatedClassOrMethod.getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            return annotatedClassOrMethod.isAnnotationPresent(PermitAll.class);
        } else {
            return roleAllowed(rolesAllowed, roleChecker);
        }
    }

    private boolean roleAllowed(RolesAllowed rolesAllowed, Function<String, Boolean> roleChecker) {
        for (String role : rolesAllowed.value()) {
            if (roleChecker.apply(role)) {
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
}
