package com.github.mcollovati.vertx.support;

import java.lang.reflect.InvocationTargetException;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.base.devserver.AbstractDevServerRunner;
import com.vaadin.base.devserver.ViteHandler;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.server.startup.ApplicationConfiguration;

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
        byteBuddy.redefine(ViteHandler.class)
                .method(ElementMatchers.named("getContextPath"))
                .intercept(MethodDelegation.to(ViteHandlerTarget.class)).make()
                .load(ViteHandler.class.getClassLoader(),
                        ClassReloadingStrategy.fromInstalledAgent());

    }

    public static final class ViteHandlerTarget {

        public static String getContextPath(@This ViteHandler source)
                throws NoSuchFieldException, InvocationTargetException,
                IllegalAccessException {
            ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) ReflectTools
                    .getJavaFieldValue(source, AbstractDevServerRunner.class
                            .getDeclaredField("applicationConfiguration"));
            String mountPoint = applicationConfiguration.getContext()
                    .getContextParameter("mountPoint");
            if (mountPoint == null) {
                mountPoint = "";
            }
            return mountPoint.replaceFirst("^/$", "");
        }
    }

}
