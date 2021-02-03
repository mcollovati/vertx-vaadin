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

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mcollovati.vertx.vaadin.connect.auth.VaadinConnectAccessChecker;
import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.connect.Endpoint;
import com.vaadin.flow.server.connect.EndpointNameChecker;
import com.vaadin.flow.server.connect.ExplicitNullableTypeChecker;
import com.vaadin.flow.server.connect.exception.EndpointException;
import com.vaadin.flow.server.connect.exception.EndpointValidationException;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseVaadinConnectEndpointService<REQUEST, RESPONSE, CTX extends EndpointServiceContext<REQUEST, RESPONSE>>
    implements VaadinConnectEndpointService<REQUEST, RESPONSE, CTX> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertxVaadinConnectEndpointService.class);

    private static Logger getLogger() {
        return LOGGER;
    }

    final Map<String, VaadinEndpointData> vaadinEndpoints = new HashMap<>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ObjectMapper vaadinEndpointMapper;
    private final VaadinConnectAccessChecker<REQUEST> accessChecker;
    private final ExplicitNullableTypeChecker explicitNullableTypeChecker;

    protected BaseVaadinConnectEndpointService(
        ObjectMapper vaadinEndpointMapper,
        VaadinConnectEndpointRegistry endpointRegistry,
        EndpointNameChecker endpointNameChecker,
        VaadinConnectAccessChecker<REQUEST> accessChecker,
        ExplicitNullableTypeChecker explicitNullableTypeChecker) {
        Objects.requireNonNull(endpointRegistry).endpoints()
            .forEach((name, endpoint) -> validateEndpointBean(endpointNameChecker, name, endpoint));

        this.vaadinEndpointMapper = vaadinEndpointMapper != null
            ? vaadinEndpointMapper
            : createVaadinConnectObjectMapper();
        this.accessChecker = accessChecker;
        this.explicitNullableTypeChecker = explicitNullableTypeChecker;
    }


    protected abstract ObjectMapper createVaadinConnectObjectMapper();

    @Override
    public RESPONSE serveEndpoint(String endpointName, String methodName, CTX context) {
        getLogger().debug("Endpoint: {}, method: {}, request body: {}", endpointName, methodName, context.requestBody());

        VaadinEndpointData vaadinEndpointData = vaadinEndpoints.get(endpointName.toLowerCase(Locale.ENGLISH));
        if (vaadinEndpointData == null) {
            getLogger().debug("Endpoint '{}' not found", endpointName);
            return context.failWithNotFound();
        }

        Method methodToInvoke = vaadinEndpointData
            .getMethod(methodName.toLowerCase(Locale.ENGLISH)).orElse(null);
        if (methodToInvoke == null) {
            getLogger().debug("Method '{}' not found in endpoint '{}'",
                methodName, endpointName);
            return context.failWithNotFound();
        }

        try {

            // Put a VaadinRequest in the instances object so as the request is
            // available in the end-point method
            VaadinService service = VaadinService.getCurrent();
            if (service != null) {
                service.setCurrentInstances(createVaadinRequest(context.request(), service), null);
            }

            return invokeVaadinEndpointMethod(endpointName, methodName,
                methodToInvoke, vaadinEndpointData, context);
        } catch (JsonProcessingException e) {
            /*
            String errorMessage = String.format(
                "Failed to serialize endpoint '%s' method '%s' response. "
                    + "Double check method's return type or specify a custom mapper bean with qualifier '%s'",
                endpointName, methodName,
                VAADIN_ENDPOINT_MAPPER_BEAN_QUALIFIER);*/
            String errorMessage = String.format(
                "Failed to serialize endpoint '%s' method '%s' response. Double check method's return type",
                endpointName, methodName);
            getLogger().error(errorMessage, e);
            try {
                return context.failWith(createResponseErrorObject(errorMessage));
            } catch (JsonProcessingException unexpected) {
                throw new IllegalStateException(String.format(
                    "Unexpected: Failed to serialize a plain Java string '%s' into a JSON. "
                        + "Double check the provided mapper's configuration.",
                    errorMessage), unexpected);
            }
        }
    }

    protected abstract VaadinRequest createVaadinRequest(REQUEST request, VaadinService service);

    private String createResponseErrorObject(String errorMessage)
        throws JsonProcessingException {
        return vaadinEndpointMapper.writeValueAsString(Collections.singletonMap(
            EndpointException.ERROR_MESSAGE_FIELD, errorMessage));
    }

    private RESPONSE invokeVaadinEndpointMethod(String endpointName,
                                                String methodName, Method methodToInvoke,
                                                VaadinEndpointData vaadinEndpointData,
                                                CTX requestContext)
        throws JsonProcessingException {
        String checkError = accessChecker.check(methodToInvoke, requestContext.request());
        if (checkError != null) {
            return requestContext.failWithUnauthorized(createResponseErrorObject(String.format(
                "Endpoint '%s' method '%s' request cannot be accessed, reason: '%s'",
                endpointName, methodName, checkError)));
        }

        Map<String, JsonNode> requestParameters = getRequestParameters(requestContext.requestBody());
        Type[] javaParameters = getJavaParameters(methodToInvoke, resolveUserClass(vaadinEndpointData.vaadinEndpointObject.getClass()));
        if (javaParameters.length != requestParameters.size()) {
            return requestContext.failWithBadRequest(createResponseErrorObject(String.format(
                "Incorrect number of parameters for endpoint '%s' method '%s', "
                    + "expected: %s, got: %s",
                endpointName, methodName, javaParameters.length,
                requestParameters.size())));
        }

        Object[] vaadinEndpointParameters;
        try {
            vaadinEndpointParameters = getVaadinEndpointParameters(
                requestParameters, javaParameters, methodName, endpointName);
        } catch (EndpointValidationException e) {
            getLogger().debug(
                "Endpoint '{}' method '{}' received invalid response",
                endpointName, methodName, e);
            return requestContext.failWithBadRequest(vaadinEndpointMapper.writeValueAsString(e.getSerializationData()));
        }

        Set<ConstraintViolation<Object>> methodParameterConstraintViolations = validator
            .forExecutables()
            .validateParameters(vaadinEndpointData.getEndpointObject(),
                methodToInvoke, vaadinEndpointParameters);
        if (!methodParameterConstraintViolations.isEmpty()) {
            return requestContext.failWithBadRequest(vaadinEndpointMapper
                .writeValueAsString(new EndpointValidationException(
                    String.format(
                        "Validation error in endpoint '%s' method '%s'",
                        endpointName, methodName),
                    createMethodValidationErrors(
                        methodParameterConstraintViolations))
                    .getSerializationData()));
        }

        Object returnValue;
        try {
            returnValue = methodToInvoke.invoke(
                vaadinEndpointData.getEndpointObject(),
                vaadinEndpointParameters);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format(
                "Received incorrect arguments for endpoint '%s' method '%s'. "
                    + "Expected parameter types (and their order) are: '[%s]'",
                endpointName, methodName,
                listMethodParameterTypes(javaParameters));
            getLogger().debug(errorMessage, e);
            return requestContext.failWithBadRequest(createResponseErrorObject(errorMessage));
        } catch (IllegalAccessException e) {
            String errorMessage = String.format(
                "Endpoint '%s' method '%s' access failure", endpointName,
                methodName);
            getLogger().error(errorMessage, e);
            return requestContext.failWith(createResponseErrorObject(errorMessage));
        } catch (InvocationTargetException e) {
            return handleMethodExecutionError(endpointName, methodName, requestContext, e);
        }

        String implicitNullError = this.explicitNullableTypeChecker
            .checkValueForAnnotatedElement(returnValue, methodToInvoke);
        if (implicitNullError != null) {
            EndpointException returnValueException = new EndpointException(
                String.format(
                    "Unexpected return value in endpoint '%s' method '%s'. %s",
                    endpointName, methodName, implicitNullError));

            getLogger().error(returnValueException.getMessage());
            return requestContext.failWith(vaadinEndpointMapper.writeValueAsString(
                returnValueException.getSerializationData()));
        }

        Set<ConstraintViolation<Object>> returnValueConstraintViolations = validator
            .forExecutables()
            .validateReturnValue(vaadinEndpointData.getEndpointObject(),
                methodToInvoke, returnValue);
        if (!returnValueConstraintViolations.isEmpty()) {
            getLogger().error(
                "Endpoint '{}' method '{}' had returned a value that has validation errors: '{}', this might cause bugs on the client side. Fix the method implementation.",
                endpointName, methodName, returnValueConstraintViolations);
        }
        return requestContext.respondWithOk(vaadinEndpointMapper.writeValueAsString(returnValue));
    }

    private String listMethodParameterTypes(Type[] javaParameters) {
        return Stream.of(javaParameters)
            .map(Type::getTypeName).collect(Collectors.joining(", "));
    }

    private RESPONSE handleMethodExecutionError(
        String endpointName, String methodName, CTX requestContext, InvocationTargetException e)
        throws JsonProcessingException {
        if (EndpointException.class
            .isAssignableFrom(e.getCause().getClass())) {
            EndpointException endpointException = ((EndpointException) e
                .getCause());
            getLogger().debug("Endpoint '{}' method '{}' aborted the execution",
                endpointName, methodName, endpointException);
            return requestContext.failWithBadRequest(vaadinEndpointMapper.writeValueAsString(
                endpointException.getSerializationData()));
        } else {
            String errorMessage = String.format(
                "Endpoint '%s' method '%s' execution failure", endpointName,
                methodName);
            getLogger().error(errorMessage, e);
            return requestContext.failWith(createResponseErrorObject(errorMessage));
        }
    }


    private Object[] getVaadinEndpointParameters(
        Map<String, JsonNode> requestParameters, Type[] javaParameters,
        String methodName, String endpointName) {
        Object[] endpointParameters = new Object[javaParameters.length];
        String[] parameterNames = new String[requestParameters.size()];
        requestParameters.keySet().toArray(parameterNames);
        Map<String, String> errorParams = new HashMap<>();
        Set<ConstraintViolation<Object>> constraintViolations = new LinkedHashSet<>();

        for (int i = 0; i < javaParameters.length; i++) {
            Type expectedType = javaParameters[i];
            try {
                Object parameter = vaadinEndpointMapper
                    .readerFor(vaadinEndpointMapper.getTypeFactory()
                        .constructType(expectedType))
                    .readValue(requestParameters.get(parameterNames[i]));

                endpointParameters[i] = parameter;

                if (parameter != null) {
                    constraintViolations.addAll(validator.validate(parameter));
                }
            } catch (IOException e) {
                String typeName = expectedType.getTypeName();
                getLogger().error(
                    "Unable to deserialize an endpoint '{}' method '{}' "
                        + "parameter '{}' with type '{}'",
                    endpointName, methodName, parameterNames[i], typeName,
                    e);
                errorParams.put(parameterNames[i], typeName);
            }
        }

        if (errorParams.isEmpty() && constraintViolations.isEmpty()) {
            return endpointParameters;
        }
        throw getInvalidEndpointParametersException(methodName, endpointName,
            errorParams, constraintViolations);
    }

    private EndpointValidationException getInvalidEndpointParametersException(
        String methodName, String endpointName,
        Map<String, String> deserializationErrors,
        Set<ConstraintViolation<Object>> constraintViolations) {
        List<EndpointValidationException.ValidationErrorData> validationErrorData = new ArrayList<>(
            deserializationErrors.size() + constraintViolations.size());

        for (Map.Entry<String, String> deserializationError : deserializationErrors
            .entrySet()) {
            String message = String.format(
                "Unable to deserialize an endpoint method parameter into type '%s'",
                deserializationError.getValue());
            validationErrorData.add(new EndpointValidationException.ValidationErrorData(message,
                deserializationError.getKey()));
        }

        validationErrorData
            .addAll(createBeanValidationErrors(constraintViolations));

        String message = String.format(
            "Validation error in endpoint '%s' method '%s'", endpointName,
            methodName);
        return new EndpointValidationException(message,
            validationErrorData);
    }

    private List<EndpointValidationException.ValidationErrorData> createBeanValidationErrors(
        Collection<ConstraintViolation<Object>> beanConstraintViolations) {
        return beanConstraintViolations.stream().map(
            constraintViolation -> new EndpointValidationException.ValidationErrorData(String.format(
                "Object of type '%s' has invalid property '%s' with value '%s', validation error: '%s'",
                constraintViolation.getRootBeanClass(),
                constraintViolation.getPropertyPath().toString(),
                constraintViolation.getInvalidValue(),
                constraintViolation.getMessage()),
                constraintViolation.getPropertyPath().toString()))
            .collect(Collectors.toList());
    }

    private List<EndpointValidationException.ValidationErrorData> createMethodValidationErrors(
        Collection<ConstraintViolation<Object>> methodConstraintViolations) {
        return methodConstraintViolations.stream().map(constraintViolation -> {
            String parameterPath = constraintViolation.getPropertyPath()
                .toString();
            return new EndpointValidationException.ValidationErrorData(String.format(
                "Method '%s' of the object '%s' received invalid parameter '%s' with value '%s', validation error: '%s'",
                parameterPath.split("\\.")[0],
                constraintViolation.getRootBeanClass(), parameterPath,
                constraintViolation.getInvalidValue(),
                constraintViolation.getMessage()), parameterPath);
        }).collect(Collectors.toList());
    }

    private Type[] getJavaParameters(Method methodToInvoke, Type classType) {
        return Stream.of(GenericTypeReflector.getExactParameterTypes(methodToInvoke, classType)).toArray(Type[]::new);
    }

    private Map<String, JsonNode> getRequestParameters(ObjectNode body) {
        Map<String, JsonNode> parametersData = new LinkedHashMap<>();
        if (body != null) {
            body.fields().forEachRemaining(entry -> parametersData
                .put(entry.getKey(), entry.getValue()));
        }
        return parametersData;
    }

    void validateEndpointBean(EndpointNameChecker endpointNameChecker,
                              String name, Object endpointBean) {

        Class<?> beanType = resolveUserClass(endpointBean.getClass());
        String endpointName = Optional
            .ofNullable(beanType.getAnnotation(Endpoint.class))
            .map(Endpoint::value).filter(value -> !value.isEmpty())
            .orElse(beanType.getSimpleName());
        if (endpointName.isEmpty()) {
            throw new IllegalStateException(String.format(
                "A bean with name '%s' and type '%s' is annotated with '%s' "
                    + "annotation but is an anonymous class hence has no name. ",
                name, beanType, Endpoint.class)
                + String.format(
                "Either modify the bean declaration so that it is not an "
                    + "anonymous class or specify an endpoint " +
                    "name in the '%s' annotation",
                Endpoint.class));
        }
        String validationError = endpointNameChecker.check(endpointName);
        if (validationError != null) {
            throw new IllegalStateException(
                String.format("Endpoint name '%s' is invalid, reason: '%s'",
                    endpointName, validationError));
        }

        vaadinEndpoints.put(endpointName.toLowerCase(Locale.ENGLISH),
            new VaadinEndpointData(endpointBean, beanType.getMethods()));
    }

    protected Class<?> resolveUserClass(Class<?> beanClass) {
        // Check the bean type instead of the implementation type in
        // case of e.g. proxies
        //Class<?> beanType = ClassUtils.getUserClass(endpointBean.getClass());
        return beanClass;
    }

    static class VaadinEndpointData {
        final Map<String, Method> methods = new HashMap<>();
        private final Object vaadinEndpointObject;

        private VaadinEndpointData(Object vaadinEndpointObject,
                                   Method... endpointMethods) {
            this.vaadinEndpointObject = vaadinEndpointObject;
            Stream.of(endpointMethods)
                .filter(method -> method.getDeclaringClass() != Object.class
                    && !method.isBridge())
                .forEach(method -> methods.put(
                    method.getName().toLowerCase(Locale.ENGLISH),
                    method));
        }

        private Optional<Method> getMethod(String methodName) {
            return Optional.ofNullable(methods.get(methodName));
        }

        private Object getEndpointObject() {
            return vaadinEndpointObject;
        }
    }

}

