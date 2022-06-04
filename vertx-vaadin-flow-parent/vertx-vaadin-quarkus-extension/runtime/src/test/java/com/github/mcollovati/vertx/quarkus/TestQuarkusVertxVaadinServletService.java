/*
 * Copyright 2000-2018 Vaadin Ltd.
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

package com.github.mcollovati.vertx.quarkus;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import java.util.Properties;

import com.github.mcollovati.vertx.quarkus.QuarkusVertxVaadinService;
import com.github.mcollovati.vertx.vaadin.VertxVaadin;
import com.github.mcollovati.vertx.vaadin.VertxVaadinContext;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinContext;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import io.vertx.core.Vertx;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestQuarkusVertxVaadinServletService
        extends QuarkusVertxVaadinService {

    private final ServletContext servletContext;
    private final String serviceName;
    private final Vertx vertx;
    private final VertxVaadinContext vaadinContext;

    public TestQuarkusVertxVaadinServletService(BeanManager beanManager,
                                                String serviceName) {
        super(mock(VertxVaadin.class),
                mock(DeploymentConfiguration.class), beanManager);
        this.serviceName = serviceName;
        servletContext = mock(ServletContext.class);
        vertx = new BeanLookup<>(beanManager, Vertx.class).lookup();
        vaadinContext = new VertxVaadinContext(vertx);

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
