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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import com.vaadin.flow.server.VaadinService;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxCsrfChecker implements CsrfChecker<RoutingContext> {

    private boolean csrfProtectionEnabled = true;

    @Override
    public boolean validateCsrfTokenInRequest(RoutingContext request) {
        if (!isCsrfProtectionEnabled()) {
            return true;
        }

        String csrfTokenInCookie = this.getCsrfTokenInCookie(request);
        if (csrfTokenInCookie == null) {
            Session session = request.session();
            if (session == null) {
                return true;
            }

            String csrfTokenInSession = session.get(VaadinService.getCsrfTokenAttributeName());
            csrfTokenInCookie = csrfTokenInSession;
        }
        if (csrfTokenInCookie == null) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Unable to verify CSRF token for endpoint request, got null token in cookie");
            }

            return false;
        } else {
            String csrfTokenInRequest = this.getCsrfTokenInRequest(request);
            if (this.compareCsrfTokens(csrfTokenInCookie, csrfTokenInRequest)) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Invalid CSRF token in endpoint request");
                }

                return false;
            } else {
                return true;
            }
        }
    }

    String getCsrfTokenInRequest(RoutingContext request) {
        return request.request().getHeader("X-CSRF-Token");
    }

    String getCsrfTokenInCookie(RoutingContext request) {
        Cookie cookie = request.request().getCookie("csrfToken");
        if(cookie!=null){
            return cookie.getValue();
        }
        return null;
    }

    private boolean compareCsrfTokens(String csrfTokenInCookie, String csrfTokenInRequest) {
        return csrfTokenInRequest == null || !MessageDigest.isEqual(csrfTokenInCookie.getBytes(StandardCharsets.UTF_8), csrfTokenInRequest.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void setCsrfProtection(boolean csrfProtectionEnabled) {
        this.csrfProtectionEnabled = csrfProtectionEnabled;
    }

    @Override
    public boolean isCsrfProtectionEnabled() {
        return csrfProtectionEnabled;
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(VertxCsrfChecker.class);
    }
}
