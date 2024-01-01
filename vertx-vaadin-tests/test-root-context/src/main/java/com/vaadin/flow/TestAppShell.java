/*
 * The MIT License
 * Copyright © 2024 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow;

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.internal.PathUtil;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.VaadinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestAppShell implements AppShellConfigurator {

    private static Logger logger = LoggerFactory.getLogger(TestAppShell.class);

    @Override
    public void configurePage(AppShellSettings settings) {

        VaadinService service = settings.getRequest().getService();
        service.getRouter()
                .getRegistry()
                .getNavigationTarget(PathUtil.trimPath(settings.getRequest().getPathInfo()))
                .flatMap(this::findTestPushAnnotation)
                .ifPresent(push -> {
                    logger.debug("Setting push {}", push.value());
                    service.addUIInitListener(event -> {
                        event.getUI().getPushConfiguration().setPushMode(push.value());
                        event.getUI().getPushConfiguration().setTransport(push.transport());
                    });
                });
    }

    private Optional<TestPush> findTestPushAnnotation(Class<? extends Component> target) {
        TestPush ann = target.getAnnotation(TestPush.class);
        ParentLayout parentLayout = target.getAnnotation(ParentLayout.class);
        if (ann == null && parentLayout != null) {
            ann = parentLayout.value().getAnnotation(TestPush.class);
        }
        return Optional.ofNullable(ann);
    }
}
