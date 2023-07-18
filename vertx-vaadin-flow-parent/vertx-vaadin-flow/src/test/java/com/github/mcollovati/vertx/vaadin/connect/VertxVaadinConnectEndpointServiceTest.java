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
package com.github.mcollovati.vertx.vaadin.connect;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.mcollovati.vertx.utils.MockServiceSessionSetup;
import com.github.mcollovati.vertx.vaadin.connect.auth.VaadinConnectAccessChecker;
import com.github.mcollovati.vertx.vaadin.connect.auth.VertxVaadinConnectAccessChecker;
import com.github.mcollovati.vertx.vaadin.connect.generator.endpoints.superclassmethods.PersonEndpoint;
import com.github.mcollovati.vertx.vaadin.connect.testendpoint.BridgeMethodTestEndpoint;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.Endpoint;
import dev.hilla.EndpointNameChecker;
import dev.hilla.EndpointRegistry;
import dev.hilla.ExplicitNullableTypeChecker;
import dev.hilla.exception.EndpointException;
import dev.hilla.exception.EndpointValidationException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Source code adapted from Vaadin Flow (https://github.com/vaadin/flow)
 */
@SuppressWarnings("unchecked")
@Ignore
public class VertxVaadinConnectEndpointServiceTest {
    private static final TestClass TEST_ENDPOINT = new TestClass();
    private static final String TEST_ENDPOINT_NAME = TEST_ENDPOINT.getClass()
            .getSimpleName();
    private static final Method TEST_METHOD;
    private static final Method TEST_VALIDATION_METHOD;
    private RoutingContext routingContextMock;
    private HttpServerRequest requestMock;
    private User principal;

    /**
     * Make the server a field instance to prevent it from garbage collected,
     * which could fail unit test randomly.
     */
    private MockServiceSessionSetup.TestVertxVaadinService service;

    static {
        TEST_METHOD = Stream.of(TEST_ENDPOINT.getClass().getDeclaredMethods())
                .filter(method -> "testMethod".equals(method.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Failed to find a test endpoint method"));
        TEST_VALIDATION_METHOD = Stream
                .of(TEST_ENDPOINT.getClass().getDeclaredMethods())
                .filter(method -> "testValidationMethod"
                        .equals(method.getName()))
                .findFirst().orElseThrow(() -> new AssertionError(
                        "Failed to find a test validation endpoint method"));
    }

    private static class TestValidationParameter {
        @Min(10)
        private final int count;

        public TestValidationParameter(@JsonProperty("count") int count) {
            this.count = count;
        }
    }

    @Endpoint
    public static class TestClass {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }

        public void testValidationMethod(
                @NotNull TestValidationParameter parameter) {
            // no op
        }

        public void testMethodWithMultipleParameter(int number, String text,
                                                    Date date) {
            // no op
        }

        @AnonymousAllowed
        public String testAnonymousMethod() {
            return "Hello, anonymous user!";
        }

        @PermitAll
        @RolesAllowed({"FOO_ROLE", "BAR_ROLE"})
        public String testRoleAllowed() {
            return "Hello, user in role!";
        }

        @DenyAll
        @AnonymousAllowed
        public void denyAll() {
        }

        @RolesAllowed("FOO_ROLE")
        @AnonymousAllowed
        public String anonymousOverrides() {
            return "Hello, no user!";
        }

        @PermitAll
        public String getUserName() {
            return VaadinService.getCurrentRequest().getUserPrincipal().getName();
        }
    }

    @Endpoint("CustomEndpoint")
    public static class TestClassWithCustomEndpointName {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }
    }

    @Endpoint("my endpoint")
    public static class TestClassWithIllegalEndpointName {
        public String testMethod(int parameter) {
            return parameter + "-test";
        }
    }

    @Endpoint
    public static class NullCheckerTestClass {
        public static final String OK_RESPONSE = "ok";

        public String testOkMethod() {
            return OK_RESPONSE;
        }

        public String testNullMethod() {
            return null;
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        routingContextMock = mock(RoutingContext.class);
        requestMock = mock(HttpServerRequest.class);
        principal = mock(User.class);

        MockServiceSessionSetup mocks = new MockServiceSessionSetup();
        service = mocks.getService();

        when(routingContextMock.request()).thenReturn(requestMock);
        when(routingContextMock.user()).thenReturn(principal);
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn("Vaadin CCDM");
        mockDenyAll();

        Session sessionMock = mock(Session.class);
        when(sessionMock.get(com.vaadin.flow.server.VaadinService.getCsrfTokenAttributeName()))
                .thenReturn("Vaadin CCDM");
        when(routingContextMock.session()).thenReturn(sessionMock);
    }

    private void mockDenyAll() {
        when(principal.isAuthorized(anyString(), any()))
                .then(i -> {
                    i.getArgument(1, Handler.class).handle(Future.succeededFuture(false));
                    return principal;
                });
    }

    @Test
    public void should_ThrowException_When_NoEndpointNameCanBeReceived() {
        TestClass anonymousClass = new TestClass() {
        };
        assertEquals("Endpoint to test should have no name",
                anonymousClass.getClass().getSimpleName(), "");

        exception.expect(IllegalStateException.class);
        exception.expectMessage("anonymous");
        exception.expectMessage(anonymousClass.getClass().getName());
        createVaadinEndpointService(anonymousClass);
    }

    @Test
    public void should_ThrowException_When_IncorrectEndpointNameProvided() {
        TestClassWithIllegalEndpointName endpointWithIllegalName =
                new TestClassWithIllegalEndpointName();
        String incorrectName = endpointWithIllegalName.getClass()
                .getAnnotation(Endpoint.class).value();
        EndpointNameChecker nameChecker = new EndpointNameChecker();
        String expectedCheckerMessage = nameChecker.check(incorrectName);
        assertNotNull(expectedCheckerMessage);

        exception.expect(IllegalStateException.class);
        exception.expectMessage(incorrectName);
        exception.expectMessage(expectedCheckerMessage);

        createVaadinEndpointService(endpointWithIllegalName, mock(ObjectMapper.class),
                null, nameChecker, null);
    }


    @Test
    public void should_Return404_When_EndpointNotFound() {
        String missingEndpointName = "whatever";
        assertNotEquals(missingEndpointName, TEST_ENDPOINT_NAME);

        DefaultVaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(missingEndpointName, null, createRequestContext(null, routingContextMock));

        assertEquals(EndpointServiceContext.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void should_Return404_When_MethodNotFound() {
        String missingEndpointMethod = "whatever";
        assertNotEquals(TEST_METHOD.getName(), missingEndpointMethod);

        DefaultVaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, missingEndpointMethod,
                        createRequestContext(null, routingContextMock));

        assertEquals(EndpointServiceContext.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void should_Return404_When_IllegalAccessToMethodIsPerformed() {
        String accessErrorMessage = "Access error";

        VaadinConnectAccessChecker<RoutingContext> restrictingCheckerMock = mock(
                VaadinConnectAccessChecker.class);
        when(restrictingCheckerMock.check(TEST_METHOD, routingContextMock))
                .thenReturn(accessErrorMessage);

        EndpointNameChecker nameCheckerMock = mock(
                EndpointNameChecker.class);
        when(nameCheckerMock.check(TEST_ENDPOINT_NAME)).thenReturn(null);

        ExplicitNullableTypeChecker explicitNullableTypeCheckerMock = mock(
                ExplicitNullableTypeChecker.class);

        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT,
                new ObjectMapper(), restrictingCheckerMock, nameCheckerMock,
                explicitNullableTypeCheckerMock)
                .serveEndpoint(TEST_ENDPOINT_NAME,
                        TEST_METHOD.getName(), createRequestContext(null, routingContextMock));

        assertEquals(EndpointServiceContext.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(accessErrorMessage));

        verify(restrictingCheckerMock, only()).check(TEST_METHOD, routingContextMock);
        verify(restrictingCheckerMock, times(1)).check(TEST_METHOD, routingContextMock);
    }

    @Test
    public void should_Return400_When_LessParametersSpecified1() {
        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestContext(null, routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("0"));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        Integer.toString(TEST_METHOD.getParameterCount())));
    }

    @Test
    public void should_Return400_When_MoreParametersSpecified() {
        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestContext(createRequestParameters(
                                "{\"value1\": 222, \"value2\": 333}"), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("2"));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        Integer.toString(TEST_METHOD.getParameterCount())));
    }

    @Test
    public void should_Return400_When_IncorrectParameterTypesAreProvided() {
        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestContext(createRequestParameters("{\"value\": [222]}"), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        TEST_METHOD.getParameterTypes()[0].getSimpleName()));
    }

    @Test
    public void should_NotCallMethod_When_UserPrincipalIsNull() {
        VertxVaadinConnectEndpointService vaadinController = createVaadinControllerWithoutPrincipal();
        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters("{\"value\": 222}"), routingContextMock));

        assertEquals(EndpointServiceContext.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull("Response body should not be null", responseBody);
        assertTrue("Should return unauthorized error",
                responseBody.contains("Unauthorized access to Vaadin endpoint"));
    }

    @Test
    public void should_CallMethodAnonymously_When_UserPrincipalIsNullAndAnonymousAllowed() {
        VertxVaadinConnectEndpointService vaadinController = createVaadinControllerWithoutPrincipal();
        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testAnonymousMethod",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        String responseBody = response.getBody();
        assertEquals("Should return message when calling anonymously",
                "\"Hello, anonymous user!\"", responseBody);
    }

    @Test
    public void should_NotCallMethod_When_a_CSRF_request() {
        when(requestMock.getHeader("X-CSRF-Token")).thenReturn(null);

        VertxVaadinConnectEndpointService vaadinController = createVaadinControllerWithoutPrincipal();
        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testAnonymousMethod",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.UNAUTHORIZED, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull("Response body should not be null", responseBody);
        assertTrue("Should return unauthorized error",
                responseBody.contains("Access denied"));
    }

    @Test
    public void should_NotCallMethodAnonymously_When_UserPrincipalIsNotInRole() {
        VertxVaadinConnectEndpointService vaadinController = createVaadinEndpointService(
                TEST_ENDPOINT, new VertxVaadinConnectAccessChecker());

        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testRoleAllowed",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Unauthorized access to Vaadin endpoint"));
    }

    @Test
    public void should_CallMethodAnonymously_When_UserPrincipalIsInRole() {
        //when(routingContextMock.isUserInRole("FOO_ROLE")).thenReturn(true);
        reset(principal);
        when(principal.isAuthorized(anyString(), any()))
                .then(i -> {
                    boolean inRole = "FOO_ROLE".equals(i.getArgument(0, String.class));
                    i.getArgument(1, Handler.class).handle(Future.succeededFuture(inRole));
                    return principal;
                });

        VertxVaadinConnectEndpointService vaadinController = createVaadinEndpointService(
                TEST_ENDPOINT, new VertxVaadinConnectAccessChecker());

        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "testRoleAllowed",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());

        assertEquals("\"Hello, user in role!\"", response.getBody());
    }

    @Test
    public void should_CallMethodAnonymously_When_AnonymousOverridesRoles() {
        VertxVaadinConnectEndpointService vaadinController = createVaadinEndpointService(
                TEST_ENDPOINT, new VertxVaadinConnectAccessChecker());

        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "anonymousOverrides",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals("\"Hello, no user!\"", response.getBody());
    }

    @Test
    public void should_NotCallMethod_When_DenyAll() {
        VertxVaadinConnectEndpointService vaadinController = createVaadinControllerWithoutPrincipal();
        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "denyAll",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals(EndpointServiceContext.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().contains("Unauthorized access to Vaadin endpoint"));
    }

    @Test
    public void should_bePossibeToGetPrincipalInEndpoint() {
        VaadinService.setCurrent(service);
        JsonObject vertxPrincipal = new JsonObject();
        vertxPrincipal.put("username", "foo");
        when(principal.principal()).thenReturn(vertxPrincipal);

        VertxVaadinConnectEndpointService vaadinController = createVaadinEndpointService(
                TEST_ENDPOINT, new VertxVaadinConnectAccessChecker());

        VaadinConnectResponse response = vaadinController.serveEndpoint(
                TEST_ENDPOINT_NAME, "getUserName",
                createRequestContext(createRequestParameters("{}"), routingContextMock));

        assertEquals("\"foo\"", response.getBody());
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsIllegalArgumentException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(inputValue,
                new IllegalArgumentException("OOPS"));

        VertxVaadinConnectEndpointService controller = createVaadinEndpointService(
                TEST_ENDPOINT);
        mockVaadinEndpoint(controller, TEST_ENDPOINT_NAME, TEST_METHOD.getName(), endpointMethodMock);

        VaadinConnectResponse response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(
                        TEST_METHOD.getParameterTypes()[0].getSimpleName()));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return500_When_EndpointMethodThrowsIllegalAccessException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(inputValue,
                new IllegalAccessException("OOPS"));

        VertxVaadinConnectEndpointService controller = createVaadinEndpointService(
                TEST_ENDPOINT);
        mockVaadinEndpoint(controller, TEST_ENDPOINT_NAME, TEST_METHOD.getName(), endpointMethodMock);

        VaadinConnectResponse response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("access failure"));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return500_When_EndpointMethodThrowsInvocationTargetException()
            throws Exception {
        int inputValue = 222;

        Method endpointMethodMock = createEndpointMethodMockThatThrows(inputValue,
                new InvocationTargetException(
                        new IllegalStateException("OOPS")));

        VertxVaadinConnectEndpointService controller = createVaadinEndpointService(
                TEST_ENDPOINT);
        mockVaadinEndpoint(controller, TEST_ENDPOINT_NAME, TEST_METHOD.getName(), endpointMethodMock);

        VaadinConnectResponse response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String responseBody = response.getBody();
        assertEndpointInfoPresent(responseBody);
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains("execution failure"));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsVaadinConnectException()
            throws Exception {
        int inputValue = 222;
        String expectedMessage = "OOPS";

        Method endpointMethodMock = createEndpointMethodMockThatThrows(inputValue,
                new InvocationTargetException(
                        new EndpointException(expectedMessage)));

        VertxVaadinConnectEndpointService controller = createVaadinEndpointService(
                TEST_ENDPOINT);
        mockVaadinEndpoint(controller, TEST_ENDPOINT_NAME, TEST_METHOD.getName(), endpointMethodMock);

        VaadinConnectResponse response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(EndpointException.class.getName()));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(expectedMessage));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    @Ignore("requires mockito version with plugin for final classes")
    public void should_Return400_When_EndpointMethodThrowsVaadinConnectExceptionSubclass()
            throws Exception {
        int inputValue = 222;
        String expectedMessage = "OOPS";

        class MyCustomException extends EndpointException {
            public MyCustomException() {
                super(expectedMessage);
            }
        }

        Method endpointMethodMock = createEndpointMethodMockThatThrows(inputValue,
                new InvocationTargetException(new MyCustomException()));

        VertxVaadinConnectEndpointService controller = createVaadinEndpointService(
                TEST_ENDPOINT);
        mockVaadinEndpoint(controller, TEST_ENDPOINT_NAME, TEST_METHOD.getName(), endpointMethodMock);

        VaadinConnectResponse response = controller.serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters(
                        String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(MyCustomException.class.getName()));
        assertTrue(String.format("Invalid response body: '%s'", responseBody),
                responseBody.contains(expectedMessage));

        verify(endpointMethodMock, times(1)).invoke(TEST_ENDPOINT, inputValue);
        verify(endpointMethodMock, times(1)).getParameters();
    }

    @Test
    public void should_Return500_When_MapperFailsToSerializeResponse()
            throws Exception {
        ObjectMapper mapperMock = mock(ObjectMapper.class);
        TypeFactory typeFactory = mock(TypeFactory.class);
        when(mapperMock.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructType(int.class))
                .thenReturn(SimpleType.constructUnsafe(int.class));
        when(mapperMock.readerFor(SimpleType.constructUnsafe(int.class)))
                .thenReturn(new ObjectMapper()
                        .readerFor(SimpleType.constructUnsafe(int.class)));

        ArgumentCaptor<Object> serializingErrorsCapture = ArgumentCaptor
                .forClass(Object.class);
        String expectedError = "expected_error";
        when(mapperMock.writeValueAsString(serializingErrorsCapture.capture()))
                .thenThrow(new JsonMappingException(null, "sss"))
                .thenReturn(expectedError);

        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT,
                mapperMock).serveEndpoint(TEST_ENDPOINT_NAME,
                TEST_METHOD.getName(),
                createRequestContext(createRequestParameters("{\"value\": 222}"), routingContextMock));

        assertEquals(EndpointServiceContext.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String responseBody = response.getBody();
        assertEquals(expectedError, responseBody);

        List<Object> passedErrors = serializingErrorsCapture.getAllValues();
        assertEquals(2, passedErrors.size());
        String lastError = passedErrors.get(1).toString();
        assertEndpointInfoPresent(lastError);
        assertTrue(String.format("Invalid response body: '%s'", lastError),
                lastError.contains(
                        "Double check"));
        //VaadinConnectController.VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER));

        verify(mapperMock, times(1))
                .readerFor(SimpleType.constructUnsafe(int.class));
        verify(mapperMock, times(2)).writeValueAsString(Mockito.isNotNull());
    }

    @Test
    public void should_ThrowException_When_MapperFailsToSerializeEverything()
            throws Exception {
        ObjectMapper mapperMock = mock(ObjectMapper.class);
        TypeFactory typeFactory = mock(TypeFactory.class);
        when(mapperMock.getTypeFactory()).thenReturn(typeFactory);
        when(typeFactory.constructType(int.class))
                .thenReturn(SimpleType.constructUnsafe(int.class));
        when(mapperMock.readerFor(SimpleType.constructUnsafe(int.class)))
                .thenReturn(new ObjectMapper()
                        .readerFor(SimpleType.constructUnsafe(int.class)));
        when(mapperMock.writeValueAsString(Mockito.isNotNull()))
                .thenThrow(new JsonMappingException(null, "sss"));

        exception.expect(IllegalStateException.class);
        exception.expectMessage("Unexpected");
        createVaadinEndpointService(TEST_ENDPOINT, mapperMock).serveEndpoint(
                TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                createRequestContext(createRequestParameters("{\"value\": 222}"), routingContextMock));
    }

    @Test
    public void should_ReturnCorrectResponse_When_EverythingIsCorrect() {
        int inputValue = 222;
        String expectedOutput = TEST_ENDPOINT.testMethod(inputValue);

        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestContext(createRequestParameters(
                                String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
                response.getBody());
    }

    /* TODO: get rid of spring Application context
    @Test
    public void should_ReturnCorrectResponse_When_EndpointClassIsProxied() {

        ApplicationContext contextMock = mock(ApplicationContext.class);
        TestClass endpoint = new TestClass();
        TestClass proxy = mock(TestClass.class, CALLS_REAL_METHODS);
        when(contextMock.getBeansWithAnnotation(Endpoint.class))
            .thenReturn(Collections.singletonMap(
                endpoint.getClass().getSimpleName(), proxy));

        VertxVaadinConnectEndpointService vaadinConnectController = null;
        /*
        VaadinConnectController vaadinConnectController = new VaadinConnectController(
            new ObjectMapper(), mock(VaadinConnectAccessChecker.class),
            mock(EndpointNameChecker.class),
            mock(ExplicitNullableTypeChecker.class),
            contextMock,
            mock(ServletContext.class));
         * /

        int inputValue = 222;
        String expectedOutput = endpoint.testMethod(inputValue);

        ResponseEntity<String> response = vaadinConnectController
            .serveEndpoint("TestClass", "testMethod",
                createRequestContext(createRequestParameters(
                    String.format("{\"value\": %s}", inputValue)),
                    routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
            response.getBody());
    }
    */

    @Test
    public void should_NotUseBridgeMethod_When_EndpointHasBridgeMethodFromInterface() {
        String inputId = "2222";
        String expectedResult = String.format("{\"id\":\"%s\"}", inputId);
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testMethodFromInterface";
        VaadinConnectResponse response = createVaadinEndpointService(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName, createRequestContext(createRequestParameters(String.format(
                                "{\"value\": {\"id\": \"%s\"}}", inputId)), routingContextMock));
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    public void should_NotUseBridgeMethod_When_EndpointHasBridgeMethodFromParentClass() {
        String inputId = "2222";
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testMethodFromClass";

        VaadinConnectResponse response = createVaadinEndpointService(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName, createRequestContext(createRequestParameters(
                                String.format("{\"value\": %s}", inputId)), routingContextMock));
        assertEquals(inputId, response.getBody());
    }

    @Test
    public void should_ReturnCorrectResponse_When_CallingNormalOverriddenMethod() {
        String inputId = "2222";
        BridgeMethodTestEndpoint.InheritedClass testEndpoint = new BridgeMethodTestEndpoint.InheritedClass();
        String testMethodName = "testNormalMethod";

        VaadinConnectResponse response = createVaadinEndpointService(testEndpoint)
                .serveEndpoint(testEndpoint.getClass().getSimpleName(),
                        testMethodName, createRequestContext(createRequestParameters(
                                String.format("{\"value\": %s}", inputId)), routingContextMock));
        assertEquals(inputId, response.getBody());
    }

    /* TODO: get rid of spring Application context
    @Test
    public void should_UseCustomEndpointName_When_ItIsDefined() {
        int input = 111;
        String expectedOutput = new TestClassWithCustomEndpointName()
            .testMethod(input);
        String beanName = TestClassWithCustomEndpointName.class.getSimpleName();

        ApplicationContext contextMock = mock(ApplicationContext.class);
        when(contextMock.getBeansWithAnnotation(Endpoint.class))
            .thenReturn(Collections.singletonMap(beanName,
                new TestClassWithCustomEndpointName()));

        VertxVaadinConnectEndpointService vaadinConnectController = null;
        /*
        VaadinConnectController vaadinConnectController = new VaadinConnectController(
            new ObjectMapper(), mock(VaadinConnectAccessChecker.class),
            mock(EndpointNameChecker.class),
            mock(ExplicitNullableTypeChecker.class),
            contextMock,
            mock(ServletContext.class));
        * /
        ResponseEntity<String> response = vaadinConnectController
            .serveEndpoint("CustomEndpoint", "testMethod",
                createRequestContext(createRequestParameters(
                    String.format("{\"value\": %s}", input)), routingContextMock));
        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
            response.getBody());
    }
    */

    /* TODO: get rid of spring Application context
    @Test
    public void should_UseCustomEndpointName_When_EndpointClassIsProxied() {

        ApplicationContext contextMock = mock(ApplicationContext.class);
        TestClassWithCustomEndpointName endpoint = new TestClassWithCustomEndpointName();
        TestClassWithCustomEndpointName proxy = mock(
            TestClassWithCustomEndpointName.class, CALLS_REAL_METHODS);
        when(contextMock.getBeansWithAnnotation(Endpoint.class))
            .thenReturn(Collections.singletonMap(
                endpoint.getClass().getSimpleName(), proxy));

        VertxVaadinConnectEndpointService vaadinConnectController = null;
        /*
        VaadinConnectController vaadinConnectController = new VaadinConnectController(
            new ObjectMapper(), mock(VaadinConnectAccessChecker.class),
            mock(EndpointNameChecker.class),
            mock(ExplicitNullableTypeChecker.class),
            contextMock,
            mock(ServletContext.class));
         * /

        int input = 111;
        String expectedOutput = endpoint.testMethod(input);

        ResponseEntity<String> response = vaadinConnectController
            .serveEndpoint("CustomEndpoint", "testMethod",
                createRequestContext(createRequestParameters(
                    String.format("{\"value\": %s}", input)),
                    routingContextMock));
        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals(String.format("\"%s\"", expectedOutput),
            response.getBody());
    }
     */

    /* TODO: remove
    @Test
    public void should_Never_UseSpringObjectMapper() {
        ApplicationContext contextMock = mock(ApplicationContext.class);
        ObjectMapper mockSpringObjectMapper = mock(ObjectMapper.class);
        ObjectMapper mockOwnObjectMapper = mock(ObjectMapper.class);
        Jackson2ObjectMapperBuilder mockObjectMapperBuilder = mock(Jackson2ObjectMapperBuilder.class);
        JacksonProperties mockJacksonProperties = mock(JacksonProperties.class);
        when(contextMock.getBean(ObjectMapper.class))
            .thenReturn(mockSpringObjectMapper);
        when(contextMock.getBean(JacksonProperties.class))
            .thenReturn(mockJacksonProperties);
        when(contextMock.getBean(Jackson2ObjectMapperBuilder.class))
            .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.createXmlMapper(false))
            .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.build())
            .thenReturn(mockOwnObjectMapper);
        when(mockJacksonProperties.getVisibility())
            .thenReturn(Collections.emptyMap());
        new VaadinConnectController(null,
            mock(VaadinConnectAccessChecker.class),
            mock(EndpointNameChecker.class),
            mock(ExplicitNullableTypeChecker.class),
            contextMock,
            mock(ServletContext.class));

        verify(contextMock, never()).getBean(ObjectMapper.class);
        verify(contextMock, times(1)).getBean(Jackson2ObjectMapperBuilder.class);
        verify(contextMock, times(1)).getBean(JacksonProperties.class);
        verify(mockOwnObjectMapper, times(1)).setVisibility(
            PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    }
    */

    /* TODO: remove
    @Test
    public void should_NotOverrideVisibility_When_JacksonPropertiesProvideVisibility() {
        ApplicationContext contextMock = mock(ApplicationContext.class);
        ObjectMapper mockDefaultObjectMapper = mock(ObjectMapper.class);
        ObjectMapper mockOwnObjectMapper = mock(ObjectMapper.class);
        Jackson2ObjectMapperBuilder mockObjectMapperBuilder = mock(Jackson2ObjectMapperBuilder.class);
        JacksonProperties mockJacksonProperties = mock(JacksonProperties.class);
        when(contextMock.getBean(ObjectMapper.class))
            .thenReturn(mockDefaultObjectMapper);
        when(contextMock.getBean(JacksonProperties.class))
            .thenReturn(mockJacksonProperties);
        when(contextMock.getBean(Jackson2ObjectMapperBuilder.class))
            .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.createXmlMapper(false))
            .thenReturn(mockObjectMapperBuilder);
        when(mockObjectMapperBuilder.build())
            .thenReturn(mockOwnObjectMapper);
        when(mockJacksonProperties.getVisibility())
            .thenReturn(Collections.singletonMap(PropertyAccessor.ALL,
                JsonAutoDetect.Visibility.PUBLIC_ONLY));
        new VaadinConnectController(null,
            mock(VaadinConnectAccessChecker.class),
            mock(EndpointNameChecker.class),
            mock(ExplicitNullableTypeChecker.class),
            contextMock,
            mock(ServletContext.class));

        verify(contextMock, never()).getBean(ObjectMapper.class);
        verify(contextMock, times(1)).getBean(Jackson2ObjectMapperBuilder.class);
        verify(mockDefaultObjectMapper, never()).setVisibility(
            PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        verify(mockOwnObjectMapper, never()).setVisibility(
            PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        verify(contextMock, times(1)).getBean(JacksonProperties.class);
    }
    */

    @Test
    public void should_ReturnValidationError_When_DeserializationFails()
            throws IOException {
        String inputValue = "\"string\"";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_METHOD.getName());
        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, TEST_METHOD.getName(),
                        createRequestContext(createRequestParameters(
                                String.format("{\"value\": %s}", inputValue)), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertEquals("value",
                validationErrorData.get("parameterName").asText());
        assertTrue(
                validationErrorData.get("message").asText().contains("'int'"));
    }

    @Test
    public void should_ReturnAllValidationErrors_When_DeserializationFailsForMultipleParameters()
            throws IOException {
        String inputValue = String.format(
                "{\"number\": %s, \"text\": %s, \"date\": %s}",
                "\"NotANumber\"", "\"ValidText\"", "\"NotADate\"");
        String testMethodName = "testMethodWithMultipleParameter";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, testMethodName);
        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME, testMethodName,
                        createRequestContext(createRequestParameters(inputValue), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);
        assertNotNull(jsonNodes);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(2, jsonNodes.get("validationErrorData").size());

        List<String> parameterNames = jsonNodes.get("validationErrorData")
                .findValuesAsText("parameterName");
        assertEquals(2, parameterNames.size());
        assertTrue(parameterNames.contains("date"));
        assertTrue(parameterNames.contains("number"));
    }

    @Test
    public void should_ReturnValidationError_When_EndpointMethodParameterIsInvalid()
            throws IOException {
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_VALIDATION_METHOD.getName());

        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME,
                        TEST_VALIDATION_METHOD.getName(),
                        createRequestContext(createRequestParameters("{\"parameter\": null}"), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertTrue(validationErrorData.get("parameterName").asText()
                .contains(TEST_VALIDATION_METHOD.getName()));
        String validationErrorMessage = validationErrorData.get("message")
                .asText();
        assertTrue(validationErrorMessage
                .contains(TEST_VALIDATION_METHOD.getName()));
        assertTrue(validationErrorMessage
                .contains(TEST_ENDPOINT.getClass().toString()));
        assertTrue(validationErrorMessage.contains("null"));
    }

    @Test
    public void should_ReturnValidationError_When_EndpointMethodBeanIsInvalid()
            throws IOException {
        int invalidPropertyValue = 5;
        String propertyName = "count";
        String expectedErrorMessage = String.format(
                "Validation error in endpoint '%s' method '%s'",
                TEST_ENDPOINT_NAME, TEST_VALIDATION_METHOD.getName());

        VaadinConnectResponse response = createVaadinEndpointService(TEST_ENDPOINT)
                .serveEndpoint(TEST_ENDPOINT_NAME,
                        TEST_VALIDATION_METHOD.getName(),
                        createRequestContext(createRequestParameters(String.format(
                                "{\"parameter\": {\"count\": %d}}",
                                invalidPropertyValue)), routingContextMock));

        assertEquals(EndpointServiceContext.BAD_REQUEST, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointValidationException.class.getName(),
                jsonNodes.get("type").asText());
        assertEquals(expectedErrorMessage, jsonNodes.get("message").asText());
        assertEquals(1, jsonNodes.get("validationErrorData").size());

        JsonNode validationErrorData = jsonNodes.get("validationErrorData")
                .get(0);
        assertTrue(validationErrorData.get("parameterName").asText()
                .contains(propertyName));
        String validationErrorMessage = validationErrorData.get("message")
                .asText();
        assertTrue(validationErrorMessage.contains(propertyName));
        assertTrue(validationErrorMessage
                .contains(Integer.toString(invalidPropertyValue)));
        assertTrue(validationErrorMessage.contains(
                TEST_VALIDATION_METHOD.getParameterTypes()[0].toString()));
    }

    @Test
    public void should_Invoke_ExplicitNullableTypeChecker()
            throws NoSuchMethodException {
        ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
                ExplicitNullableTypeChecker.class);

        when(explicitNullableTypeChecker.checkValueForAnnotatedElement(
                eq(NullCheckerTestClass.OK_RESPONSE),
                argThat(getIsStringReturnType()), anyBoolean()))
                .thenReturn(null);

        String testOkMethod = "testOkMethod";
        VaadinConnectResponse response = createVaadinEndpointService(
                new NullCheckerTestClass(), null, null, null,
                explicitNullableTypeChecker).serveEndpoint(
                NullCheckerTestClass.class.getSimpleName(),
                testOkMethod, createRequestContext(createRequestParameters("{}"),
                        routingContextMock));

        verify(explicitNullableTypeChecker).checkValueForAnnotatedElement(
                NullCheckerTestClass.OK_RESPONSE,
                NullCheckerTestClass.class.getMethod(testOkMethod), true);

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals("\"" + NullCheckerTestClass.OK_RESPONSE + "\"",
                response.getBody());
    }


    @Test
    public void should_ReturnException_When_ExplicitNullableTypeChecker_ReturnsError()
            throws IOException, NoSuchMethodException {
        final String errorMessage = "Got null";

        ExplicitNullableTypeChecker explicitNullableTypeChecker = mock(
                ExplicitNullableTypeChecker.class);
        String testNullMethodName = "testNullMethod";
        Method testNullMethod = NullCheckerTestClass.class
                .getMethod(testNullMethodName);
        when(explicitNullableTypeChecker.checkValueForAnnotatedElement(null,
                testNullMethod, true))
                .thenReturn(errorMessage);

        VaadinConnectResponse response = createVaadinEndpointService(
                new NullCheckerTestClass(), null, null, null,
                explicitNullableTypeChecker).serveEndpoint(
                NullCheckerTestClass.class.getSimpleName(),
                testNullMethodName, createRequestContext(createRequestParameters("{}"), routingContextMock));

        verify(explicitNullableTypeChecker).checkValueForAnnotatedElement(null,
                testNullMethod, true);

        assertEquals(EndpointServiceContext.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ObjectNode jsonNodes = new ObjectMapper().readValue(response.getBody(),
                ObjectNode.class);

        assertEquals(EndpointException.class.getName(),
                jsonNodes.get("type").asText());
        final String message = jsonNodes.get("message").asText();
        assertTrue(message.contains("Unexpected return value"));
        assertTrue(message.contains(NullCheckerTestClass.class.getSimpleName()));
        assertTrue(message.contains(testNullMethodName));
        assertTrue(message.contains(errorMessage));
    }

    @Test
    public void should_ReturnResult_When_CallingSuperClassMethodWithGenericTypedParameter() {
        DefaultVaadinConnectResponse response = createVaadinEndpointService(new PersonEndpoint())
                .serveEndpoint(PersonEndpoint.class.getSimpleName(), "update",
                        createRequestContext(createRequestParameters(
                                "{\"entity\":{\"name\":\"aa\"}}"), routingContextMock));

        assertEquals(EndpointServiceContext.OK, response.getStatusCode());
        assertEquals("{\"name\":\"aa\"}", response.getBody());
    }

    private void assertEndpointInfoPresent(String responseBody) {
        assertTrue(String.format(
                "Response body '%s' should have endpoint information in it",
                responseBody), responseBody.contains(TEST_ENDPOINT_NAME));
        assertTrue(String.format(
                "Response body '%s' should have endpoint information in it",
                responseBody), responseBody.contains(TEST_METHOD.getName()));
    }

    private ObjectNode createRequestParameters(String jsonBody) {
        try {
            return Json.decodeValue(jsonBody, ObjectNode.class);
        } catch (DecodeException e) {
            throw new AssertionError(String
                    .format("Failed to deserialize the json: %s", jsonBody), e);
        }
    }

    private <T> VertxVaadinConnectEndpointService createVaadinEndpointService(T endpoint) {
        return createVaadinEndpointService(endpoint, null, null, null, null);
    }

    private <T> VertxVaadinConnectEndpointService createVaadinEndpointService(T endpoint,
                                                                              ObjectMapper vaadinEndpointMapper) {
        return createVaadinEndpointService(endpoint, vaadinEndpointMapper, null, null, null);
    }

    private <T> VertxVaadinConnectEndpointService createVaadinEndpointService(T endpoint,
                                                                              VaadinConnectAccessChecker<RoutingContext> accessChecker) {
        return createVaadinEndpointService(endpoint, null, accessChecker, null, null);
    }

    private <T> VertxVaadinConnectEndpointService createVaadinEndpointService(T endpoint,
                                                                              ObjectMapper vaadinEndpointMapper,
                                                                              VaadinConnectAccessChecker<RoutingContext> accessChecker,
                                                                              EndpointNameChecker endpointNameChecker,
                                                                              ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        if (vaadinEndpointMapper == null) {
            vaadinEndpointMapper = new ObjectMapper();
        }

        if (accessChecker == null) {
            accessChecker = mock(
                    VaadinConnectAccessChecker.class);
            when(accessChecker.check(eq(TEST_METHOD), eq(routingContextMock))).thenReturn(null);
        }

        if (endpointNameChecker == null) {
            endpointNameChecker = mock(EndpointNameChecker.class);
            when(endpointNameChecker.check(TEST_ENDPOINT_NAME)).thenReturn(null);
        }
        VaadinEndpointRegistry endpointRegistry = new VertxEndpointRegistry(endpointNameChecker);
        endpointRegistry.registerEndpoint(endpoint);

        if (explicitNullableTypeChecker == null) {
            explicitNullableTypeChecker = mock(
                    ExplicitNullableTypeChecker.class);
            when(explicitNullableTypeChecker.checkValueForAnnotatedElement(any(), any(), anyBoolean()))
                    .thenReturn(null);
        }

        return new VertxVaadinConnectEndpointService(vaadinEndpointMapper, endpointRegistry, accessChecker, explicitNullableTypeChecker);
    }

    private VertxVaadinConnectEndpointService createVaadinControllerWithoutPrincipal() {
        when(routingContextMock.user()).thenReturn(null);
        return createVaadinEndpointService(TEST_ENDPOINT, new VertxVaadinConnectAccessChecker());
    }

    @SuppressWarnings("unchecked")
    private static VertxConnectRequestContext createRequestContext(
            ObjectNode body, RoutingContext routingContext
    ) {
        return new VertxConnectRequestContext(routingContext, body);
    }

    private Method createEndpointMethodMockThatThrows(Object argument,
                                                      Exception exceptionToThrow) throws Exception {
        Method endpointMethodMock = mock(Method.class);
        when(endpointMethodMock.invoke(TEST_ENDPOINT, argument))
                .thenThrow(exceptionToThrow);
        when(endpointMethodMock.getParameters())
                .thenReturn(TEST_METHOD.getParameters());
        doReturn(TEST_METHOD.getDeclaringClass()).when(endpointMethodMock)
                .getDeclaringClass();
        when(endpointMethodMock.getParameterTypes())
                .thenReturn(TEST_METHOD.getParameterTypes());
        when(endpointMethodMock.getName()).thenReturn(TEST_METHOD.getName());
        return endpointMethodMock;
    }

    private ArgumentMatcher<AnnotatedElement> getIsStringReturnType() {
        return new ArgumentMatcher<AnnotatedElement>() {
            @Override
            public boolean matches(AnnotatedElement argument) {
                return argument instanceof Method && String.class.equals(((Method) argument).getGenericReturnType());
            }
        };
    }

    private static void mockVaadinEndpoint(VertxVaadinConnectEndpointService service, String endpointName, String methodName, Method method) {
        EndpointRegistry.VaadinEndpointData endpointData = service.endpointRegistry.get(endpointName.toLowerCase());
        try {
            Field methodsField = EndpointRegistry.VaadinEndpointData.class.getDeclaredField("methods");
            methodsField.setAccessible(true);
            Map<String, Method> methodMap = (Map<String, Method>) methodsField.get(endpointData);
            methodMap.put(methodName.toLowerCase(), method);
        } catch (Exception aE) {
            throw new AssertionError("Cannot mock endpoint " + endpointName + "." + methodName);
        }

    }
}
