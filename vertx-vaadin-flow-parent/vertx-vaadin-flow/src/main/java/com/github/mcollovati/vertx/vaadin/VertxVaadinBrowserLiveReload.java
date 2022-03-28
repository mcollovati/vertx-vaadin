/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin;

import com.github.mcollovati.vertx.vaadin.communication.VertxDebugWindowConnection;
import com.github.mcollovati.vertx.vaadin.sockjs.communication.VertxVaadinLiveReload;
import com.vaadin.base.devserver.BrowserLiveReloadAccessorImpl;
import com.vaadin.flow.internal.BrowserLiveReload;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinService;
import org.atmosphere.cpr.AtmosphereResource;

class VertxVaadinBrowserLiveReload implements BrowserLiveReload {

    private final BrowserLiveReload delegate;
    private final VertxVaadinLiveReload reloader;

    VertxVaadinBrowserLiveReload(BrowserLiveReload delegate, VertxVaadinLiveReload reloader) {
        this.delegate = delegate;
        this.reloader = reloader;
    }

    @Override
    public Backend getBackend() {
        return delegate.getBackend();
    }

    @Override
    public void setBackend(Backend backend) {
        delegate.setBackend(backend);
    }

    @Override
    public void onConnect(AtmosphereResource resource) {
        throw new UnsupportedOperationException("Not supported on vertx-vaadin");
    }

    @Override
    public void onDisconnect(AtmosphereResource resource) {
        throw new UnsupportedOperationException("Not supported on vertx-vaadin");
    }

    @Override
    public boolean isLiveReload(AtmosphereResource resource) {
        throw new UnsupportedOperationException("Not supported on vertx-vaadin");
    }

    @Override
    public void onMessage(String msg) {
        delegate.onMessage(msg);
    }

    @Override
    public void reload() {
        reloader.reload();
    }

    public static class Accessor extends BrowserLiveReloadAccessorImpl {

        @Override
        public BrowserLiveReload getLiveReload(VaadinService service) {
            service.getContext().getAttribute(
                VertxDebugWindowConnection.class, () -> new VertxDebugWindowConnection((VertxVaadinService) service)
            );
            return super.getLiveReload(service);
        }

        @Override
        public BrowserLiveReload getLiveReload(VaadinContext context) {
            VertxDebugWindowConnection connection = context.getAttribute(
                VertxDebugWindowConnection.class, VertxDebugWindowConnection::new);
            return context.getAttribute(Holder.class, () -> new Holder(
                new VertxVaadinBrowserLiveReload(super.getLiveReload(context), connection)
            )).liveReload;
        }
    }

    private static final class Holder {
        private final BrowserLiveReload liveReload;

        private Holder(BrowserLiveReload liveReload) {
            this.liveReload = liveReload;
        }
    }

}
