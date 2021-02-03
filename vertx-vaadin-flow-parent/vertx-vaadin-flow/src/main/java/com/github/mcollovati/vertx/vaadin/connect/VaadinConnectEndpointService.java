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

import com.vaadin.flow.server.connect.Endpoint;

/**
 * The service that is responsible for processing Vaadin Connect requests.
 * Each class that is annotated with {@link Endpoint} gets its public methods
 * exposed so that those can be triggered by a correct POST request, including
 * the methods inherited from the other classes, excluding {@link Object} class
 * ones. Other methods (non-public) are not considered by the controller.
 * <p>
 * For example, if a class with name {@code TestClass} that has the only public
 * method {@code testMethod} was annotated with the annotation, it can be called
 * via {@literal http://${base_url}/testclass/testmethod} POST call, where
 * {@literal ${base_url}} is the application base url, configured by the user.
 * Class name and method name case in the request URL does not matter, but if
 * the method has parameters, the request body should contain a valid JSON with
 * all parameters in the same order as they are declared in the method. The
 * parameter types should also correspond for the request to be successful.
 *
 * @param <REQUEST>  type of the request which triggers the endpoint call
 * @param <RESPONSE> type of the response container
 * @param <CTX>      type of the processing context carrying request data
 */
public interface VaadinConnectEndpointService<REQUEST, RESPONSE, CTX extends EndpointServiceContext<REQUEST, RESPONSE>> {

    /**
     * Processes the Vaadin Connect requests.
     * <p>
     * Matches the endpoint name and a method name with the corresponding Java
     * class and a public method in the class. Extracts parameters from a
     * request body if the Java method requires any and applies in the same
     * order. After the method call, serializes the Java method execution result
     * and sends it back.
     * <p>
     * If an issue occurs during the request processing, an error response is
     * returned instead of the serialized Java method return value.
     *
     * @param endpointName the name of an endpoint to address the calls to, not case
     *                     sensitive
     * @param methodName   the method name to execute on an endpoint, not case sensitive
     * @param context      request context, encapsulating the current request which triggers the endpoint call
     *                     and the optional request body, that should be present if the method
     *                     called has parameters
     * @return execution result as a JSON string or an error message string wrapped into the response container
     */
    RESPONSE serveEndpoint(String endpointName, String methodName, CTX context);
}
