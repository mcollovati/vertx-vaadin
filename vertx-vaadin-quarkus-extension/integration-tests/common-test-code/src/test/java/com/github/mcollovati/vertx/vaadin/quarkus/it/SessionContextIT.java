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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mcollovati.vertx.vaadin.quarkus.it.sessioncontext.SessionContextView;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.github.mcollovati.vertx.vaadin.quarkus.it.sessioncontext.SessionContextView.SessionScopedBean.DESTROY_COUNT;

@QuarkusIntegrationTest
public class SessionContextIT extends AbstractCdiIT {

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open();
    }

    @Test
    public void sameSessionIsAccessibleFromUIs() {
        assertLabelEquals("");
        click(SessionContextView.SETVALUEBTN_ID);
        getDriver().navigate().refresh(); // creates new UI
        assertLabelEquals(SessionContextView.VALUE);
    }

    @Test
    public void httpSessionCloseDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.HTTP_INVALIDATEBTN_ID);
        assertDestroyCountEquals(1);
    }

    @Test
    @Tag("slow")
    @Disabled("Session timeout cannot be updated at runtime on Vert.x")
    public void httpSessionExpirationDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.EXPIREBTN_ID);
        boolean destroyed = false;
        getLogger().info("Waiting for session expiration...");
        for (int i = 0; i < 60; i++) {
            Thread.sleep(1000);
            if (getCount(DESTROY_COUNT) > 0) {
                getLogger().info("session expired after {} seconds", i);
                destroyed = true;
                break;
            }
        }
        assertTrue(destroyed);
    }

    @Override
    protected String getTestPath() {
        return "/session";
    }

    private void assertLabelEquals(String expected) {
        assertTextEquals(expected, SessionContextView.VALUELABEL_ID);
    }

    private void assertDestroyCountEquals(int expectedCount) throws IOException {
        assertCountEquals(expectedCount, DESTROY_COUNT);
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(SessionContextIT.class);
    }
}
