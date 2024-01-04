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

import java.util.Properties;
import jakarta.enterprise.inject.spi.CDI;

import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import org.mockito.Mockito;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/*
 * NOTE: this code has been copy/pasted and adapted from vaadin-quarkus extension, credit goes to Vaadin Ltd.
 */

public class SessionUnderTestContext implements UnderTestContext {

    private VaadinSession session;
    private static ServiceUnderTestContext serviceUnderTestContext;

    private void mockSession() {
        if (serviceUnderTestContext == null) {
            serviceUnderTestContext = new ServiceUnderTestContext(CDI.current().getBeanManager());
            serviceUnderTestContext.activate();
        }
        session = Mockito.mock(TestSession.class, Mockito.withSettings().useConstructor());
        doCallRealMethod().when(session).setAttribute(Mockito.any(String.class), Mockito.any());
        doCallRealMethod().when(session).getAttribute(Mockito.any(String.class));
        doCallRealMethod().when(session).getService();

        when(session.getState()).thenReturn(VaadinSessionState.OPEN);

        when(session.hasLock()).thenReturn(true);
        DeploymentConfiguration configuration = Mockito.mock(DeploymentConfiguration.class);
        when(session.getConfiguration()).thenReturn(configuration);
        Properties props = new Properties();
        when(configuration.getInitParameters()).thenReturn(props);

        doCallRealMethod().when(session).addUI(Mockito.any());
        doCallRealMethod().when(session).getUIs();

        Mockito.doAnswer(invocation -> {
                    invocation.getArgument(0, Command.class).execute();
                    return null;
                })
                .when(session)
                .access(Mockito.any());
    }

    @Override
    public void activate() {
        if (session == null) {
            mockSession();
        }
        VaadinSession.setCurrent(session);
    }

    @Override
    public void tearDownAll() {
        VaadinSession.setCurrent(null);
        if (serviceUnderTestContext != null) {
            serviceUnderTestContext.tearDownAll();
            serviceUnderTestContext = null;
        }
    }

    @Override
    public void destroy() {
        if (session != null) {
            session.getService().fireSessionDestroy(session);
        }
    }

    public VaadinSession getSession() {
        return session;
    }

    public static class TestSession extends VaadinSession {

        public TestSession() {
            super(serviceUnderTestContext.getService());
        }
    }
}
