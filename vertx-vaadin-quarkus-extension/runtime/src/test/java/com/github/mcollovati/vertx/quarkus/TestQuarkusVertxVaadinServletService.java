/*
 * The MIT License
 * Copyright © 2000-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.quarkus;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.servlet.ServletContext;
import java.util.Properties;

import com.vaadin.flow.di.Lookup;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.startup.ApplicationConfiguration;
import io.vertx.core.Vertx;
import org.mockito.Mockito;

import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestQuarkusVertxVaadinServletService extends QuarkusVertxVaadinService {

    private final ServletContext servletContext;
    private final String serviceName;
    private final Vertx vertx;
    private final VertxVaadinContext vaadinContext;

    public TestQuarkusVertxVaadinServletService(BeanManager beanManager, String serviceName) {
        super(mock(VertxVaadin.class), mock(DeploymentConfiguration.class), beanManager);
        this.serviceName = serviceName;
        servletContext = mock(ServletContext.class);
        vertx = new BeanLookup<>(beanManager, Vertx.class).lookup();
        vaadinContext = new VertxVaadinContext(vertx);

        vaadinContext.setAttribute(Lookup.class, Mockito.mock(Lookup.class));
        vaadinContext.setAttribute(ApplicationConfiguration.class, Mockito.mock(ApplicationConfiguration.class));

        DeploymentConfiguration config = getDeploymentConfiguration();
        Properties properties = new Properties();
        when(config.getInitParameters()).thenReturn(properties);
    }

    @Override
    protected VertxVaadinContext constructVaadinContext() {
        return vaadinContext;
    }

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getMainDivId(VaadinSession session, VaadinRequest request) {
        return "test-1";
    }

    // We have nothing to do with atmosphere,
    // and mocking is much easier without it.
    @Override
    protected boolean isAtmosphereAvailable() {
        return false;
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            super.setClassLoader(classLoader);
        }
    }
}
