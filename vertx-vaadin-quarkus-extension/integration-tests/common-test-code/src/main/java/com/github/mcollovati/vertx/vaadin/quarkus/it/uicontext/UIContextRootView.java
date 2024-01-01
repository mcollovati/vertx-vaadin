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
package com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UIScopedLabel.SetTextEvent;

@Route("ui")
public class UIContextRootView extends Div {

    public static final String CLOSE_UI_BTN = "CLOSE_UI_BTN";
    public static final String CLOSE_SESSION_BTN = "CLOSE_SESSION_BTN";
    public static final String TRIGGER_EVENT_BTN = "TRIGGER_EVENT_BTN";
    public static final String INJECTER_LINK = "injecter view";
    public static final String UISCOPED_LINK = "uiscoped view";
    public static final String UIID_LABEL = "UIID_LABEL";
    public static final String NORMALSCOPED_LINK = "normalscoped bean view";
    public static final String EVENT_PAYLOAD = "EVENT_PAYLOAD";

    public static final String UI_SCOPED_BEAN_ID = "ui-scoped-bean";

    @Inject
    UIScopedBean bean;

    @Inject
    UIScopedLabel label;

    @Inject
    Event<SetTextEvent> setTextEventTrigger;

    @PostConstruct
    private void init() {
        final String uiIdStr = UI.getCurrent().getUIId() + "";
        label.setText(uiIdStr);

        final Label uiId = new Label(uiIdStr);
        uiId.setId(UIID_LABEL);

        final NativeButton closeUI = new NativeButton("close UI", event -> getUI().ifPresent(UI::close));
        closeUI.setId(CLOSE_UI_BTN);

        final NativeButton closeSession = new NativeButton("close session", event -> getUI().ifPresent(
                        ui -> ui.getSession().close()));
        closeSession.setId(CLOSE_SESSION_BTN);

        final NativeButton triggerEvent =
                new NativeButton("event trigger", event -> setTextEventTrigger.fire(new SetTextEvent(EVENT_PAYLOAD)));
        triggerEvent.setId(TRIGGER_EVENT_BTN);

        Div beanDiv = new Div();
        beanDiv.setId(UI_SCOPED_BEAN_ID);
        beanDiv.setText(bean.getId());

        final Div navDiv = new Div(
                new RouterLink(INJECTER_LINK, UIScopeInjecterView.class),
                new RouterLink(UISCOPED_LINK, UIScopedView.class),
                new RouterLink(NORMALSCOPED_LINK, UINormalScopedBeanView.class));

        add(new Div(uiId), new Div(closeUI, closeSession), new Div(triggerEvent), new Div(this.label), navDiv, beanDiv);
    }
}
