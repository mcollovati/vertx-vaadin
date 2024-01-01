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

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.LocationChangeEvent;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.Router;
import io.quarkus.arc.Arc;
import net.bytebuddy.ByteBuddy;
import org.mockito.Mockito;

import com.github.mcollovati.vertx.quarkus.context.RouteScopedContext.NavigationData;

public class RouteUnderTestContext implements UnderTestContext {

    private static UIUnderTestContext uiContextUnderTest;

    private UI ui;

    private static int INDEX = 0;

    private void mockUI() {
        if (uiContextUnderTest == null) {
            uiContextUnderTest = new UIUnderTestContext();
            uiContextUnderTest.activate();
        }
        ui = uiContextUnderTest.getUi();
    }

    @Override
    public void activate() {
        if (ui == null) {
            mockUI();
        }
        Class<?> clazz = new ByteBuddy()
                .subclass(TestNavigationTarget.class)
                .name(TestNavigationTarget.class.getName() + "_" + INDEX + "$")
                .make()
                .load(TestNavigationTarget.class.getClassLoader())
                .getLoaded();
        INDEX++;
        UI.setCurrent(ui);
        NavigationData data = new NavigationData(clazz, Collections.emptyList());
        ComponentUtil.setData(ui, NavigationData.class, data);
    }

    @Override
    public void tearDownAll() {
        UI.setCurrent(null);
        if (uiContextUnderTest != null) {
            uiContextUnderTest.tearDownAll();
            uiContextUnderTest = null;
        }
    }

    @Override
    public void destroy() {
        // throw new UnsupportedOperationException(
        // "The underlying context cannot be destroyed using mocks. Destroy
        // context via calling API methods");
        List<HasElement> newNavigation = Collections.singletonList(new TestHasElement());
        Arc.container()
                .beanManager()
                .fireEvent(new AfterNavigationEvent(new LocationChangeEvent(
                        Mockito.mock(Router.class),
                        ui,
                        NavigationTrigger.PROGRAMMATIC,
                        new Location("foo"),
                        newNavigation)));
    }
}
