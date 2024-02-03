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

import java.lang.reflect.Method;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
/**
 * Source code adapted from Vaadin Flow (https://github.com/vaadin/flow)
 */
public class VertxConnectAccessCheckerTest {
    private static final String ROLE_USER = "ROLE_USER";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private VertxVaadinConnectAccessChecker checker;
    private RoutingContext routingContextMock;
    private HttpServerRequest requestMock;
    private Session sessionMock;

    @Before
    public void before() {
        checker = new VertxVaadinConnectAccessChecker();

        routingContextMock = mock(RoutingContext.class);
        requestMock = mock(HttpServerRequest.class);
        sessionMock = mock(Session.class);
        when(sessionMock.get(VaadinService.getCsrfTokenAttributeName())).thenReturn("Vaadin CCDM");
        when(routingContextMock.request()).thenReturn(requestMock);
        when(routingContextMock.session()).thenReturn(sessionMock);
        User user = mock(User.class);
        when(routingContextMock.user()).thenReturn(user);
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn("Vaadin CCDM");
        when(user.isAuthorized(anyString(), any())).then(i -> {
            boolean inRole = "ROLE_USER".equals(i.getArgument(0, String.class));
            i.getArgument(1, Handler.class).handle(Future.succeededFuture(inRole));
            return user;
        });
    }

    private void createAnonymousContext() {
        when(routingContextMock.user()).thenReturn(null);
    }

    private void createDifferentSessionToken() {
        when(sessionMock.get(VaadinService.getCsrfTokenAttributeName())).thenReturn("CCDM Token");
    }

    private void createNullTokenContextInHeaderRequest() {
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn(null);
    }

    private void createNullTokenSession() {
        when(sessionMock.get(VaadinService.getCsrfTokenAttributeName())).thenReturn(null);
    }

    private void createNullSession() {
        when(routingContextMock.session()).thenReturn(null);
    }

    private void shouldPass(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNull(checker.check(method, routingContextMock));
    }

    private void shouldFail(Class<?> test) throws Exception {
        Method method = test.getMethod("test");
        assertNotNull(checker.check(method, routingContextMock));
    }

    @Test
    public void should_fail_When_not_having_token_in_headerRequest() throws Exception {
        class Test {
            public void test() {}
        }
        createNullTokenContextInHeaderRequest();
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_not_having_token_in_session_but_have_token_in_request_header() throws Exception {
        class Test {
            public void test() {}
        }
        createNullTokenSession();
        shouldFail(Test.class);
    }

    @Test
    public void should_fail_When_not_having_token_in_session_but_have_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {}
        }
        createNullTokenSession();
        shouldFail(Test.class);
    }

    @Test
    public void should_pass_When_not_having_session_And_not_having_token_in_request_header() throws Exception {
        @PermitAll
        class Test {
            public void test() {}
        }
        createNullSession();
        createNullTokenContextInHeaderRequest();
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_not_having_session_And_not_having_token_in_request_header_And_AnonymousAllowed()
            throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {}
        }
        createNullSession();
        createNullTokenContextInHeaderRequest();
        shouldPass(Test.class);
    }

    @Test
    public void should_pass_When_csrf_disabled() throws Exception {
        @PermitAll
        class Test {
            public void test() {}
        }
        createNullTokenContextInHeaderRequest();
        checker.enableCsrf(false);
        shouldPass(Test.class);
    }

    @Test
    public void should_fail_When_having_different_token_between_session_and_headerRequest() throws Exception {
        class Test {
            public void test() {}
        }
        createDifferentSessionToken();
        shouldFail(Test.class);
    }

    @Test
    public void
            should_fail_When_having_different_token_between_session_and_headerRequest_and_NoAuthentication_AnonymousAllowed()
                    throws Exception {
        class Test {
            @AnonymousAllowed
            public void test() {}
        }
        createAnonymousContext();
        createDifferentSessionToken();
        shouldFail(Test.class);
    }

    @Test
    public void should_Fail_When_NoAuthentication() throws Exception {
        class Test {
            public void test() {}
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_Pass_When_Authentication_And_matching_token() throws Exception {
        @PermitAll
        class Test {
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_Fail_When_DenyAllClass() throws Exception {
        @DenyAll
        class Test {
            public void test() {}
        }
        shouldFail(Test.class);
    }

    @Test()
    public void should_Pass_When_DenyAllClass_ValidRoleMethod() throws Exception {
        @DenyAll
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_DenyAllClass_PermitAllMethod() throws Exception {
        @DenyAll
        class Test {
            @PermitAll
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Fail_When_InvalidRoleClass() throws Exception {
        @RolesAllowed({"ROLE_ADMIN"})
        class Test {
            public void test() {}
        }
        shouldFail(Test.class);
    }

    @Test()
    public void should_Pass_When_InvalidRoleClass_ValidRoleMethod() throws Exception {
        @RolesAllowed({"ROLE_ADMIN"})
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_InvalidRoleClass_PermitAllMethod() throws Exception {
        @RolesAllowed({"ROLE_ADMIN"})
        class Test {
            @PermitAll
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test()
    public void should_Pass_When_ValidRoleClass() throws Exception {
        @RolesAllowed(ROLE_USER)
        class Test {
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_ClassIsAnnotated() throws Exception {
        @AnonymousAllowed
        class Test {
            public void test() {}
        }

        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_MethodIsAnnotated() throws Exception {
        class Test {
            @AnonymousAllowed
            public void test() {}
        }
        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_NotAllowAnonymousAccess_When_NoAnnotationsPresent() throws Exception {
        class Test {
            public void test() {}
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_AllowAnyAuthenticatedAccess_When_PermitAllAndAnonymousAllowed() throws Exception {
        class Test {
            @PermitAll
            @AnonymousAllowed
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_PermitAllAndAnonymousAllowed() throws Exception {
        class Test {
            @PermitAll
            @AnonymousAllowed
            public void test() {}
        }

        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnyAuthenticatedAccess_When_RolesAllowedAndAnonymousAllowed() throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @AnonymousAllowed
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_AllowAnonymousAccess_When_RolesAllowedAndAnonymousAllowed() throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @AnonymousAllowed
            public void test() {}
        }
        createAnonymousContext();
        shouldPass(Test.class);
    }

    @Test
    public void should_DisallowAnyAuthenticatedAccess_When_DenyAllAndAnonymousAllowed() throws Exception {
        class Test {
            @DenyAll
            @AnonymousAllowed
            public void test() {}
        }
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowNotMatchingRoleAccess_When_RolesAllowedAndPermitAll() throws Exception {
        class Test {
            @RolesAllowed("ADMIN")
            @PermitAll
            public void test() {}
        }
        shouldFail(Test.class);
    }

    @Test
    public void should_AllowSpecificRoleAccess_When_RolesAllowedAndPermitAll() throws Exception {
        class Test {
            @RolesAllowed(ROLE_USER)
            @PermitAll
            public void test() {}
        }
        shouldPass(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_DenyAllAndAnonymousAllowed() throws Exception {
        class Test {
            @DenyAll
            @AnonymousAllowed
            public void test() {}
        }
        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithDenyAll() throws Exception {
        @AnonymousAllowed
        class Test {
            @DenyAll
            public void test() {}
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithRolesAllowed() throws Exception {
        @AnonymousAllowed
        class Test {
            @RolesAllowed(ROLE_USER)
            public void test() {}
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_DisallowAnonymousAccess_When_AnonymousAllowedIsOverriddenWithPermitAll() throws Exception {
        @AnonymousAllowed
        class Test {
            @PermitAll
            public void test() {}
        }

        createAnonymousContext();
        shouldFail(Test.class);
    }

    @Test
    public void should_Throw_When_PrivateMethodIsPassed() throws Exception {
        class Test {
            private void test() {}
        }

        Method method = Test.class.getDeclaredMethod("test");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(method.toString());
        checker.check(method, routingContextMock);
    }
}
