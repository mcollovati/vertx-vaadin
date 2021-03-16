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

import com.vaadin.flow.server.VaadinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Basic implementation of {@link VaadinConnectAccessChecker}.
 * <p>
 * Subclasses will provide technology specific ways to access
 * session and user information.
 * <p>
 * Source adapted from Vaadin Flow (https://github.com/vaadin/flow) fusion-endpoints module,
 * to get rid of servlet APIs.
 *
 * @param <REQUEST> request type
 */
public abstract class VaadinConnectAccessCheckerSupport<REQUEST> implements VaadinConnectAccessChecker<REQUEST> {

    public static final String ACCESS_DENIED_MSG = "Access denied";

    public static final String ACCESS_DENIED_MSG_DEV_MODE = "Unauthorized access to Vaadin endpoint; "
            + "to enable endpoint access use one of the following annotations: @AnonymousAllowed, @PermitAll, @RolesAllowed";

    private CsrfChecker<REQUEST> csrfChecker;

    private AccessAnnotationChecker<REQUEST> accessAnnotationChecker;


    /**
     * Creates a new instance.
     *
     * @param csrfChecker             the csrf checker to use
     * @param accessAnnotationChecker the access checker to use
     */
    protected VaadinConnectAccessCheckerSupport(
            AccessAnnotationChecker<REQUEST> accessAnnotationChecker,
            CsrfChecker<REQUEST> csrfChecker) {
        this.accessAnnotationChecker = accessAnnotationChecker;
        this.csrfChecker = csrfChecker;
    }

    /**
     * Check that the endpoint is accessible for the current user.
     *
     * @param method  the Vaadin endpoint method to check ACL
     * @param request the request that triggers the <code>method</code> invocation
     * @return an error String with an issue description, if any validation
     * issues occur, {@code null} otherwise
     */
    public String check(Method method, REQUEST request) {
        if (!csrfChecker.validateCsrfTokenInRequest(request)) {
            return ACCESS_DENIED_MSG;
        }

        if (accessAnnotationChecker.hasAccess(method, request)) {
            return null;
        }

        if (isDevMode()) {
            // suggest access control annotations in dev mode
            return ACCESS_DENIED_MSG_DEV_MODE;
        } else {
            return ACCESS_DENIED_MSG;
        }
    }

    private boolean isDevMode() {
        VaadinService vaadinService = VaadinService.getCurrent();
        return (vaadinService != null && !vaadinService
                .getDeploymentConfiguration().isProductionMode());
    }

    /**
     * Enable or disable XSRF token checking in endpoints.
     *
     * @param xsrfProtectionEnabled enable or disable protection.
     */
    public void enableCsrf(boolean xsrfProtectionEnabled) {
        csrfChecker.setCsrfProtection(xsrfProtectionEnabled);
    }

    /**
     * Returns the instance used for checking access based on annotations.
     *
     * @return the instance used for checking access based on annotations
     */
    public AccessAnnotationChecker<REQUEST> getAccessAnnotationChecker() {
        return accessAnnotationChecker;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VaadinConnectAccessChecker.class);
    }

}
