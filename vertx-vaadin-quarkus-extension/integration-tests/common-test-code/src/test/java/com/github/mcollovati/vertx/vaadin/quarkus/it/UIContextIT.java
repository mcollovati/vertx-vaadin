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
package com.github.mcollovati.vertx.vaadin.quarkus.it;

import java.io.IOException;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UIContextRootView;
import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UINormalScopedBeanView;
import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UIScopedBean;
import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UIScopedLabel;
import com.github.mcollovati.vertx.vaadin.quarkus.it.uicontext.UIScopedView;

@QuarkusIntegrationTest
public class UIContextIT extends AbstractCdiIT {

    private String uiId;

    @Override
    protected String getTestPath() {
        return "/ui";
    }

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open();
        uiId = getText(UIContextRootView.UIID_LABEL);
    }

    @Test
    public void beanDestroyedOnUIClose() throws IOException {
        assertCountEquals(UIScopedBean.DESTROY_COUNTER_KEY, 0);
        click(UIContextRootView.CLOSE_UI_BTN);
        assertCountEquals(UIScopedBean.DESTROY_COUNTER_KEY, 1);
    }

    @Test
    public void beanDestroyedOnSessionClose() throws IOException, InterruptedException {
        assertCountEquals(UIScopedBean.DESTROY_COUNTER_KEY, 0);
        click(UIContextRootView.CLOSE_SESSION_BTN);

        assertCountEquals(UIScopedBean.DESTROY_COUNTER_KEY, 1);
    }

    @Test
    public void viewSurvivesNavigation() {
        follow(UIContextRootView.UISCOPED_LINK);
        assertTextEquals("", UIScopedView.VIEWSTATE_LABEL);
        click(UIScopedView.SETSTATE_BTN);
        assertTextEquals(UIScopedView.UISCOPED_STATE, UIScopedView.VIEWSTATE_LABEL);
        follow(UIScopedView.ROOT_LINK);
        follow(UIContextRootView.UISCOPED_LINK);
        assertTextEquals(UIScopedView.UISCOPED_STATE, UIScopedView.VIEWSTATE_LABEL);
    }

    @Test
    public void sameScopedComponentInjectedInOtherView() {
        String beanId = getText(UIContextRootView.UI_SCOPED_BEAN_ID);
        assertTextEquals(uiId, UIScopedLabel.ID);
        follow(UIContextRootView.INJECTER_LINK);
        assertTextEquals(beanId, UIContextRootView.UI_SCOPED_BEAN_ID);
        assertTextEquals(uiId, UIScopedLabel.ID);
    }

    @Test
    public void observerCalledOnInstanceAttachedToUI() {
        click(UIContextRootView.TRIGGER_EVENT_BTN);
        assertTextEquals(UIContextRootView.EVENT_PAYLOAD, UIScopedLabel.ID);
    }

    @Test
    public void normalScopedBeanInjectedToLargerScopeChangesWithActiveUI() {
        follow(UIContextRootView.NORMALSCOPED_LINK);
        assertTextEquals(uiId, UINormalScopedBeanView.UIID_LABEL);
        open();
        uiId = getText(UIContextRootView.UIID_LABEL);
        follow(UIContextRootView.NORMALSCOPED_LINK);
        assertTextEquals(uiId, UINormalScopedBeanView.UIID_LABEL);
    }

    private void assertCountEquals(String key, int expectedCount) throws IOException {
        assertCountEquals(expectedCount, key + uiId);
    }
}
