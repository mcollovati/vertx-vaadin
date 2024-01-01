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

import java.lang.reflect.Modifier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.slf4j.LoggerFactory;

public class HillaWorkAround {

    static final String SPRINGFRAMEWORK_CLASS_UTILS = "org.springframework.util.ClassUtils";

    public static void install() {
        try {
            Class.forName(SPRINGFRAMEWORK_CLASS_UTILS);
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(HillaWorkAround.class)
                    .warn(
                            "Spring Framework not present in classpath, installing workaround to support Fusion endpoints");
            // Create a fake ClassUtils class to avoid spring framework dependency
            new ByteBuddy()
                    .subclass(Object.class)
                    .name(SPRINGFRAMEWORK_CLASS_UTILS)
                    .defineMethod("getUserClass", Class.class, Modifier.PUBLIC | Modifier.STATIC)
                    .withParameters(Class.class)
                    .intercept(MethodDelegation.to(HillaWorkAround.class))
                    .make()
                    .load(HillaWorkAround.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION);
        }
    }

    public static final String CGLIB_CLASS_SEPARATOR = "$$";

    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }
}
