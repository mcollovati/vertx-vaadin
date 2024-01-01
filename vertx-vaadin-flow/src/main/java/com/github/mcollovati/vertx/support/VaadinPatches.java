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

import java.lang.reflect.InvocationTargetException;

import com.vaadin.base.devserver.AbstractDevServerRunner;
import com.vaadin.base.devserver.ViteHandler;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Patches for Vaadin code that cannot be overridden in a better way. For
 * examples private methods with explicit casts to servlet based Vaadin
 * components.
 */
public class VaadinPatches {

    public static void patch() {
        /*
        ByteBuddyAgent.install();
        ByteBuddy byteBuddy = new ByteBuddy();
        patchViteHandler(byteBuddy);
         */
    }

    static void patchViteHandler(ByteBuddy byteBuddy) {
        byteBuddy
                .redefine(ViteHandler.class)
                .method(ElementMatchers.named("getContextPath"))
                .intercept(MethodDelegation.to(ViteHandlerTarget.class))
                .make()
                .load(ViteHandler.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    public static final class ViteHandlerTarget {

        public static String getContextPath(@This ViteHandler source)
                throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
            ApplicationConfiguration applicationConfiguration =
                    (ApplicationConfiguration) ReflectTools.getJavaFieldValue(
                            source, AbstractDevServerRunner.class.getDeclaredField("applicationConfiguration"));
            String mountPoint = applicationConfiguration.getContext().getContextParameter("mountPoint");
            if (mountPoint == null) {
                mountPoint = "";
            }
            return mountPoint.replaceFirst("^/$", "");
        }
    }
}
