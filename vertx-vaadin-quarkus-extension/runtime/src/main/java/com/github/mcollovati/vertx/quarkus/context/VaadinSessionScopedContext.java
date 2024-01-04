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

import java.lang.annotation.Annotation;
import jakarta.enterprise.context.spi.Contextual;

import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionDestroyListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;

/**
 * Context for {@link VaadinSessionScoped @VaadinSessionScoped} beans.
 * <p>
 * Stores contextuals in {@link VaadinSession}. Other Vaadin CDI contexts are
 * stored in the corresponding {@link VaadinSessionScoped} context.
 *
 * @since 1.0
 *
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */
public class VaadinSessionScopedContext extends AbstractContext {
    private static final String ATTRIBUTE_NAME = VaadinSessionScopedContext.class.getName();

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        VaadinSession session = VaadinSession.getCurrent();
        ContextualStorage storage = findContextualStorage(session);
        if (storage == null && createIfNotExist) {
            storage = new SessionContextualStorage(session);
            session.setAttribute(ATTRIBUTE_NAME, storage);
        }
        return storage;
    }

    private static ContextualStorage findContextualStorage(VaadinSession session) {
        // session lock is checked inside
        return (ContextualStorage) session.getAttribute(ATTRIBUTE_NAME);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return VaadinSessionScoped.class;
    }

    @Override
    public boolean isActive() {
        return VaadinSession.getCurrent() != null;
    }

    private static class SessionContextualStorage extends ContextualStorage implements SessionDestroyListener {

        private final Registration registration;

        private final VaadinSession session;

        private SessionContextualStorage(VaadinSession session) {
            super(false);
            this.session = session;
            registration = session.getService().addSessionDestroyListener(this);
        }

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            if (!session.equals(event.getSession())) {
                return;
            }
            getLogger().debug("VaadinSessionScopedContext destroy");
            ContextualStorage storage = findContextualStorage(event.getSession());
            registration.remove();
            if (storage != null) {
                AbstractContext.destroyAllActive(storage);
            }
        }

        private Logger getLogger() {
            return LoggerFactory.getLogger(SessionContextualStorage.class);
        }
    }
}
