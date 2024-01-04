/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.enterprise.context.NormalScope;

import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The lifecycle of a NormalRouteScoped bean is controlled by route navigation.
 * <p>
 * Every NormalRouteScoped bean belongs to one router component owner. It can be
 * a {@link Route @Route}, or a {@link RouterLayout}, or a
 * {@link HasErrorParameter HasErrorParameter}. Beans are qualified by
 * {@link RouteScopeOwner @RouteScopeOwner} to link with their owner.
 * <p>
 * Until owner remains active, all beans owned by it remain in the scope.
 * <p>
 * You cannot use this scope with Vaadin Components. Proxy Components do not
 * work correctly within the Vaadin framework, so as a precaution the Vaadin CDI
 * plugin will not deploy if any such beans are discovered.
 * <p>
 * The sister annotation to this is the {@link RouteScoped}. Both annotations
 * reference the same underlying scope, so it is possible to get both a proxy
 * and a direct reference to the same object by using different annotations.
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
@NormalScope
@Inherited
@Target({ANNOTATION_TYPE, TYPE, FIELD, METHOD, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface NormalRouteScoped {}
