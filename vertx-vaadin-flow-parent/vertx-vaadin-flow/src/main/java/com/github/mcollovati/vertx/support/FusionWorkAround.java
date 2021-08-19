package com.github.mcollovati.vertx.support;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

public class FusionWorkAround {

    static final String SPRINGFRAMEWORK_CLASS_UTILS = "org.springframework.util.ClassUtils";

    public static void install() {
        try {
            Class.forName(SPRINGFRAMEWORK_CLASS_UTILS);
        } catch (ClassNotFoundException e) {
            LoggerFactory.getLogger(FusionWorkAround.class).warn("Spring Framework not present in classpath, installing workaround to support Fusion endpoints");
            // Create a fake ClassUtils class to avoid spring framework dependency
            new ByteBuddy().subclass(Object.class)
                    .name(SPRINGFRAMEWORK_CLASS_UTILS)
                    .defineMethod("getUserClass", Class.class, Modifier.PUBLIC | Modifier.STATIC)
                    .withParameters(Class.class)
                    .intercept(MethodDelegation.to(FusionWorkAround.class))
                    .make().load(FusionWorkAround.class.getClassLoader(), ClassLoadingStrategy.Default.INJECTION);
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
