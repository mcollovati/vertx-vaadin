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
package com.github.mcollovati.vertx.vaadin.quarkus.it.sessioncontext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import com.github.mcollovati.vertx.quarkus.annotation.VaadinSessionScoped;
import com.github.mcollovati.vertx.vaadin.quarkus.it.Counter;

@Route("session")
public class SessionContextView extends Div {

    public static final String SETVALUEBTN_ID = "setvalbtn";
    public static final String VALUELABEL_ID = "label";
    public static final String VALUE = "session";
    public static final String INVALIDATEBTN_ID = "invalidatebtn";
    public static final String HTTP_INVALIDATEBTN_ID = "httpinvalidatebtn";
    public static final String EXPIREBTN_ID = "expirebtn";

    @Inject
    private SessionScopedBean sessionScopedBean;

    @PostConstruct
    private void init() {
        NativeButton setBtn = new NativeButton("set");
        setBtn.addClickListener(event -> sessionScopedBean.setValue(VALUE));
        setBtn.setId(SETVALUEBTN_ID);
        add(setBtn);

        NativeButton invalidateBtn = new NativeButton("invalidate");
        invalidateBtn.addClickListener(event -> VaadinSession.getCurrent().close());
        invalidateBtn.setId(INVALIDATEBTN_ID);
        add(invalidateBtn);

        NativeButton httpInvalidateBtn = new NativeButton("httpinvalidate");
        httpInvalidateBtn.addClickListener(
                event -> VaadinSession.getCurrent().getSession().invalidate());
        httpInvalidateBtn.setId(HTTP_INVALIDATEBTN_ID);
        add(httpInvalidateBtn);

        NativeButton expireBtn = new NativeButton("httpexpire");
        expireBtn.addClickListener(
                event -> VaadinSession.getCurrent().getSession().setMaxInactiveInterval(1));
        expireBtn.setId(EXPIREBTN_ID);
        add(expireBtn);

        Label label = new Label();
        label.setText(sessionScopedBean.getValue()); // bean instantiated here
        label.setId(VALUELABEL_ID);
        add(label);
    }

    @VaadinSessionScoped
    // Like other vaadin scopes,
    // Serializable is mandatory only if you want a working session
    // serialization
    public static class SessionScopedBean {
        public static final String DESTROY_COUNT = "SessionScopedBeanDestroy";

        @Inject
        Counter counter;

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @PreDestroy
        private void preDestroy() {
            counter.increment(DESTROY_COUNT);
        }
    }
}
