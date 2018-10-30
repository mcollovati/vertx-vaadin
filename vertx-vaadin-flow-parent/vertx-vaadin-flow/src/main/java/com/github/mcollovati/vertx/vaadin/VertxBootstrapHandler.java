/*
 * The MIT License
 * Copyright © 2016-2018 Marco Collovati (mcollovati@gmail.com)
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;

import com.vaadin.flow.server.BootstrapHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import io.vertx.core.http.HttpServerRequest;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

public class VertxBootstrapHandler extends BootstrapHandler {

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        return super.synchronizedHandleRequest(session, MockVaadinRequest.createMock(request), response);
    }
}

class MockVaadinRequest extends VaadinServletRequest {

    @Delegate(excludes = Excludes.class)
    VaadinRequest request;

    /**
     * Wraps a http servlet request and associates with a vaadin service.
     *
     * @param request       the http servlet request to wrap
     * @param vaadinService
     */
    public MockVaadinRequest(VaadinRequest delegate, HttpServletRequest request, VaadinServletService service) {
        super(request, service);
        this.request = delegate;
    }


    @Override
    public String getServletPath() {
        return "";
    }


    static MockVaadinRequest createMock(VaadinRequest request) {
        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
            MockVaadinRequest.class.getClassLoader(),
            new Class[]{HttpServletRequest.class},
            (proxy, method, args) -> { return null; }
        );
        return new MockVaadinRequest(request, req, new MockServletService(request.getService()));
    }

    interface Excludes {
        InputStream getInputStream() throws IOException;

        HttpServerRequest getRequest();

        VertxVaadinService getService();
    }
}

@RequiredArgsConstructor
class MockServletService extends VaadinServletService {
    @Delegate
    private final VaadinService service;

}