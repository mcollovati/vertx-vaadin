/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.support;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static com.github.mcollovati.vertx.support.HillaWorkAround.SPRINGFRAMEWORK_CLASS_UTILS;

public class HillaWorkAroundUT {

    @Test
    public void testFusionWorkAround() throws Throwable {
        HillaWorkAround.install();
        Class<?> classUtils = Class.forName(SPRINGFRAMEWORK_CLASS_UTILS);

        assertThat(classUtils).as("class loaded").isNotNull();

        Class<?> testClass = HillaWorkAround.class;
        Class<?> result = invokeGetUserClass(classUtils, testClass);
        assertThat(result).isEqualTo(testClass);
    }

    private Class<?> invokeGetUserClass(Class<?> classUtils, Class<?> testClass) throws Throwable {
        return (Class<?>) MethodHandles.lookup()
                .findStatic(classUtils, "getUserClass", MethodType.methodType(Class.class, Class.class))
                .invoke(testClass);
    }
}
