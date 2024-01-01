/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.vertx;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.vaadin.flow.component.Component;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class ViewClassLocator {
    private final LinkedHashMap<String, Class<? extends Component>> views = new LinkedHashMap<>();

    public ViewClassLocator(ClassLoader classLoader) {
        try (ScanResult scanResult = new ClassGraph()
                .whitelistPackages("com.vaadin.flow.uitest")
                .enableClassInfo()
                .enableMethodInfo()
                .ignoreClassVisibility()
                .addClassLoader(classLoader)
                .scan()) {

            //noinspection unchecked
            scanResult
                    .getSubclasses(Component.class.getCanonicalName())
                    .filter(classInfo -> !classInfo.isAbstract()
                            // Only include views which have a no-arg
                            // constructor
                            && !classInfo.isInnerClass()
                            && classInfo
                                            .getConstructorInfo()
                                            .filter(methodInfo -> methodInfo.getParameterInfo().length == 0)
                                            .size()
                                    == 1)
                    .loadClasses(true)
                    .forEach(cls -> views.put(cls.getSimpleName(), (Class<? extends Component>) cls));
        }
    }

    public Collection<Class<? extends Component>> getAllViewClasses() {
        return views.values();
    }
}
