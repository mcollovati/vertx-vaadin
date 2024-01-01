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

import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.github.mcollovati.vertx.vaadin.quarkus.it.service.BootstrapCustomizer;
import com.github.mcollovati.vertx.vaadin.quarkus.it.service.ServiceView;
import com.github.mcollovati.vertx.vaadin.quarkus.it.service.TestErrorHandler;
import com.github.mcollovati.vertx.vaadin.quarkus.it.service.TestSystemMessagesProvider;

@QuarkusIntegrationTest
public class ServiceIT extends AbstractCdiIT {

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
    }

    @Override
    protected String getTestPath() {
        return "/service";
    }

    @Test
    public void bootstrapCustomizedByServiceInitEventObserver() {
        getDriver().get(getRootURL() + "/bootstrap");
        waitForDevServer();

        assertTextEquals(BootstrapCustomizer.APPENDED_TXT, BootstrapCustomizer.APPENDED_ID);
    }

    @Test
    public void serviceScopedBeanIsPreservedAcrossUIs() throws IOException {
        open();

        String id = getText("service-id");

        // open another UI
        open();

        Assertions.assertTrue(
                id.endsWith("-1"),
                "The number of created beans must be 1 but it's " + id.substring(id.indexOf("-") + 1));
        Assertions.assertEquals(id, getText("service-id"));
    }

    @Test
    public void sessionExpiredMessageCustomized() {
        open();
        click(ServiceView.EXPIRE);
        click(ServiceView.ACTION);
        assertSystemMessageEquals(TestSystemMessagesProvider.EXPIRED_BY_TEST);
    }

    @Test
    public void errorHandlerCustomized() throws IOException {
        String counter = TestErrorHandler.class.getSimpleName();
        assertCountEquals(0, counter);
        open();
        click(ServiceView.FAIL);
        assertCountEquals(1, counter);
    }

    @Test
    public void sessionInitEventObserved() throws IOException {
        String initCounter = SessionInitEvent.class.getSimpleName();
        assertCountEquals(0, initCounter);
        getDriver().manage().deleteAllCookies();
        open();
        assertCountEquals(1, initCounter);
    }

    @Test
    public void sessionDestroyEventObserved() throws IOException {
        String destroyCounter = SessionDestroyEvent.class.getSimpleName();
        assertCountEquals(0, destroyCounter);
        open();
        assertCountEquals(0, destroyCounter);
        click(ServiceView.EXPIRE);
        assertCountEquals(1, destroyCounter);
    }

    @Test
    public void uiInitEventObserved() throws IOException {
        String uiInitCounter = UIInitEvent.class.getSimpleName();
        assertCountEquals(0, uiInitCounter);
        open();
        assertCountEquals(1, uiInitCounter);
    }

    private void assertSystemMessageEquals(String expected) {
        WebElement message = findElement(By.cssSelector("div.v-system-error div.message"));
        Assertions.assertEquals(expected, message.getText());
    }
}
