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

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

public class UIUnderTestContext implements UnderTestContext {

    private VaadinSession session;
    private UI ui;
    private static int uiIdNdx = 0;
    private static SessionUnderTestContext sessionContextUnderTest;

    public UIUnderTestContext() {
        this(null);
    }

    protected UIUnderTestContext(VaadinSession session) {
        this.session = session;
    }

    private void mockUI() {
        if (session == null) {
            mockSession();
        }

        ui = new UI();
        ui.getInternals().setSession(session);
        uiIdNdx++;
        ui.doInit(null, uiIdNdx);
    }

    private void mockSession() {
        if (sessionContextUnderTest == null) {
            sessionContextUnderTest = new SessionUnderTestContext();
            sessionContextUnderTest.activate();
        }
        session = sessionContextUnderTest.getSession();
    }

    @Override
    public void activate() {
        if (ui == null) {
            mockUI();
        }
        UI.setCurrent(ui);
    }

    @Override
    public void tearDownAll() {
        UI.setCurrent(null);
        uiIdNdx = 0;
        if (sessionContextUnderTest != null) {
            sessionContextUnderTest.tearDownAll();
            sessionContextUnderTest = null;
        }
    }

    @Override
    public void destroy() {
        ComponentUtil.onComponentDetach(ui);
    }

    public UI getUi() {
        return ui;
    }
}
