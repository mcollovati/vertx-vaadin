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
package com.github.mcollovati.vertx.quarkus.context;

import java.util.Set;
import javax.enterprise.context.Dependent;

import com.vaadin.flow.server.VaadinSession;
import io.quarkus.arc.Unremovable;

import com.github.mcollovati.vertx.quarkus.context.RouteScopedContext.ContextualStorageManager;
import com.github.mcollovati.vertx.quarkus.context.RouteScopedContext.RouteStorageKey;

/**
 *
 * Quarkus will use newly created instance of ContextualStorageManager to fire
 * navigation events. As a result <code>@Observes</code> methods will be called
 * on instance which is not used by any other piece of the code. To be able to
 * call it on the correct instance it's retreived from the session attribute:
 * it's the valid way since the bean is in fact session scoped (originally).
 */
@Dependent
@Unremovable
public class TestContextualStorageManager extends ContextualStorageManager {
    @Override
    protected Set<RouteStorageKey> getKeySet() {
        TestContextualStorageManager manager = (TestContextualStorageManager) VaadinSession.getCurrent()
                .getAttribute(BeanManagerProxy.getAttributeName(TestContextualStorageManager.class));
        if (manager != this) {
            return manager.getKeySet();
        }
        return super.getKeySet();
    }

    @Override
    protected void destroy(RouteStorageKey key) {
        TestContextualStorageManager manager = (TestContextualStorageManager) VaadinSession.getCurrent()
                .getAttribute(BeanManagerProxy.getAttributeName(TestContextualStorageManager.class));
        if (manager != this) {
            manager.destroy(key);
        } else {
            super.destroy(key);
        }
    }
}
