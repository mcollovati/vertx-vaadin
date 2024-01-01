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

/**
 * Handles checking of a CSRF token in endpoint requests.
 *
 * @param <R> request type
 */
public interface CsrfChecker<R> {

    /**
     * Validates the CSRF token that is included in the request.
     * <p>
     * Checks that the CSRF token in the request matches the expected one that
     * is stored in the HTTP session.
     * <p>
     * Note! If there is no session, this method will always return
     * {@code true}.
     * <p>
     * Note! If CSRF protection is disabled, this method will always return
     * {@code true}.
     *
     * @param request the request to validate
     * @return {@code true} if the CSRF token is ok or checking is disabled or
     * there is no HTTP session, {@code false} otherwise
     */
    boolean validateCsrfTokenInRequest(R request);

    /**
     * Enable or disable CSRF token checking in endpoints.
     *
     * @param csrfProtectionEnabled enable or disable protection
     */
    void setCsrfProtection(boolean csrfProtectionEnabled);

    /**
     * Checks if CSRF token checking in endpoints is enabled.
     *
     * @return {@code true} if protection is enabled, {@code false} otherwise
     */
    boolean isCsrfProtectionEnabled();
}
