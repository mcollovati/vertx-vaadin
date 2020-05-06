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
package com.github.mcollovati.vertx.vaadin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.server.Constants;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Test borrowed from  vaadin codebae: VaadinServletContextTest
public class VertxVaadinContextTest {

    private VertxVaadinContext context;

    private static String testAttributeProvider() {
        return "RELAX_THIS_IS_A_TEST";
    }

    private final Map<String, Object> attributeMap = new HashMap<>();
    private Map<String, String> properties;

    @Before
    public void setUp() {
        Vertx vertx = mock(Vertx.class);
        Context vertxContext = mock(Context.class);
        when(vertx.getOrCreateContext()).thenReturn(vertxContext);
        doAnswer(i -> attributeMap.put(i.getArgumentAt(0, String.class), i.getArguments()[1])).when(vertxContext).put(anyString(), any());
        doAnswer(i -> attributeMap.remove(i.getArgumentAt(0, String.class)) != null).when(vertxContext).remove(anyString());
        when(vertxContext.get(anyString())).thenAnswer(i -> attributeMap.get(i.getArgumentAt(0, String.class)));

        properties = new HashMap<>();
        properties.put(Constants.SERVLET_PARAMETER_PRODUCTION_MODE, "true");
        properties.put(Constants.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false");

        JsonObject opts = new JsonObject();
        properties.forEach(opts::put);
        VaadinOptions vaadinOptions = new VaadinOptions(opts);

        context = new VertxVaadinContext(vertx, vaadinOptions);
    }

    @Test
    public void getAttributeWithProvider() {
        Assert.assertNull(context.getAttribute(String.class));

        String value = context.getAttribute(String.class,
            VertxVaadinContextTest::testAttributeProvider);
        Assert.assertEquals(testAttributeProvider(), value);

        Assert.assertEquals("Value from provider should be persisted",
            testAttributeProvider(), context.getAttribute(String.class));
    }

    @Test(expected = AssertionError.class)
    public void setNullAttributeNotAllowed() {
        context.setAttribute(null);
    }

    @Test
    public void getMissingAttributeWithoutProvider() {
        String value = context.getAttribute(String.class);
        Assert.assertNull(value);
    }

    @Test
    public void setAndGetAttribute() {
        String value = testAttributeProvider();
        context.setAttribute(value);
        String result = context.getAttribute(String.class);
        Assert.assertEquals(value, result);
        // overwrite
        String newValue = "this is a new value";
        context.setAttribute(newValue);
        result = context.getAttribute(String.class);
        Assert.assertEquals(newValue, result);
        // now the provider should not be called, so value should be still there
        result = context.getAttribute(String.class, () -> {
            throw new AssertionError("Should not be called");
        });
        Assert.assertEquals(newValue, result);
    }

    @Test
    public void setValueBasedOnSuperType_implicitClass_notFound() {
        String value = testAttributeProvider();
        context.setAttribute(value);

        CharSequence retrieved = context.getAttribute(CharSequence.class);
        Assert.assertNull(
            "Value set base on its own type should not be found based on a super type",
            retrieved);
    }

    @Test
    public void setValueBasedOnSuperType_explicitClass_found() {
        String value = testAttributeProvider();
        context.setAttribute(CharSequence.class, value);

        CharSequence retrieved = context.getAttribute(CharSequence.class);
        Assert.assertSame(
            "Value should be found based on the type used when setting",
            value, retrieved);
    }

    @Test
    public void removeValue_removeMethod_valueIsRemoved() {
        context.setAttribute(testAttributeProvider());
        context.removeAttribute(String.class);

        Assert.assertNull("Value should be removed",
            context.getAttribute(String.class));
    }

    @Test
    public void removeValue_setWithClass_valueIsRemoved() {
        context.setAttribute(testAttributeProvider());
        context.setAttribute(String.class, null);

        Assert.assertNull("Value should be removed",
            context.getAttribute(String.class));
    }

    @Test
    public void getPropertyNames_returnsExpectedProperties() {
        List<String> list = Collections
            .list(context.getContextParameterNames());
        Assert.assertEquals(
            "Context should return only keys defined in ServletContext",
            properties.size(), list.size());
        for (String key : properties.keySet()) {
            Assert.assertEquals(String.format(
                "Value should be same from context for key '%s'", key),
                properties.get(key), context.getContextParameter(key));
        }
    }
}
