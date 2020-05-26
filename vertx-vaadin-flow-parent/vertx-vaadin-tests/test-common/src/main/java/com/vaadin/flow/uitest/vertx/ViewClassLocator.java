/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
            .overrideClassLoaders(classLoader)
            .scan()) {

            //noinspection unchecked
            scanResult.getSubclasses(Component.class.getCanonicalName())
                .filter(classInfo -> !classInfo.isAbstract()
                    // Only include views which have a no-arg
                    // constructor
                    && classInfo.getConstructorInfo().
                    filter(methodInfo -> methodInfo.getParameterInfo().length == 0).size() == 1
                ).loadClasses(true)
                .forEach(cls -> views.put(cls.getSimpleName(), (Class<? extends Component>) cls));
        }
    }

    public Collection<Class<? extends Component>> getAllViewClasses() {
        return views.values();
    }

}
