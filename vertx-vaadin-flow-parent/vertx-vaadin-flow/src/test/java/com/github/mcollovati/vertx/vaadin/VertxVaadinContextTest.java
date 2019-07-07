package com.github.mcollovati.vertx.vaadin;

import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
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

    @Before
    public void setUp() {
        Vertx vertx = mock(Vertx.class);
        Context vertxContext = mock(Context.class);
        when(vertx.getOrCreateContext()).thenReturn(vertxContext);
        doAnswer(i -> attributeMap.put(i.getArgumentAt(0, String.class), i.getArguments()[1])).when(vertxContext).put(anyString(), any());
        doAnswer(i -> attributeMap.remove(i.getArgumentAt(0, String.class)) != null ).when(vertxContext).remove(anyString());
        when(vertxContext.get(anyString())).thenAnswer(i -> attributeMap.get(i.getArgumentAt(0, String.class)));
        context = new VertxVaadinContext(vertx);
    }

    @Test
    public void getAttributeWithProvider() {
        Assert.assertNull(context.getAttribute(String.class));

        String value = context.getAttribute(String.class,
            VertxVaadinContextTest::testAttributeProvider);
        assertEquals(testAttributeProvider(), value);

        assertEquals("Value from provider should be persisted",
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
        assertEquals(value, result);
        // overwrite
        String newValue = "this is a new value";
        context.setAttribute(newValue);
        result = context.getAttribute(String.class);
        assertEquals(newValue, result);
        // now the provider should not be called, so value should be still there
        result = context.getAttribute(String.class,
            () -> {
                throw new AssertionError("Should not be called");
            });
        assertEquals(newValue, result);
    }

    @Test
    public void removeAttribute() {
        TestObj value = new TestObj();
        context.setAttribute(value);
        assertEquals(value, context.getAttribute(TestObj.class));
        context.removeAttribute(TestObj.class);
        assertNull(context.getAttribute(TestObj.class));
    }

    private static class TestObj {}
}
