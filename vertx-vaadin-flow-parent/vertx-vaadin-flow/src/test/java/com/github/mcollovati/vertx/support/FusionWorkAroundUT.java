package com.github.mcollovati.vertx.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.github.mcollovati.vertx.support.FusionWorkAround.SPRINGFRAMEWORK_CLASS_UTILS;


public class FusionWorkAroundUT {

    @Test
    public void testFusionWorkAround() throws Throwable {
        FusionWorkAround.install();
        Class<?> classUtils = Class.forName(SPRINGFRAMEWORK_CLASS_UTILS);

        assertThat(classUtils).as("class loaded").isNotNull();

        Class<?> testClass = FusionWorkAround.class;
        Class<?> result = invokeGetUserClass(classUtils, testClass);
        assertThat(result).isEqualTo(testClass);
    }

    private Class<?> invokeGetUserClass(Class<?> classUtils, Class<?> testClass) throws Throwable {
        return (Class<?>) MethodHandles.lookup()
                .findStatic(classUtils, "getUserClass", MethodType.methodType(Class.class, Class.class))
                .invoke(testClass);
    }
}