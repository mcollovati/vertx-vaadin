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

import com.github.mcollovati.vertx.vaadin.quarkus.it.sessioncontext.SessionContextView;

import static com.github.mcollovati.vertx.vaadin.quarkus.it.sessioncontext.SessionContextView.SessionScopedBean.DESTROY_COUNT;

@QuarkusIntegrationTest
public class SessionContextCloseIT extends AbstractCdiIT {

    @Override
    protected String getTestPath() {
        return "/session";
    }

    @BeforeEach
    public void setUp() throws Exception {
        resetCounts();
        open();
    }

    @Test
    public void vaadinSessionCloseDestroysSessionContext() throws Exception {
        assertDestroyCountEquals(0);
        click(SessionContextView.INVALIDATEBTN_ID);
        assertDestroyCountEquals(1);
    }

    private void assertDestroyCountEquals(int expectedCount) throws IOException {
        assertCountEquals(expectedCount, DESTROY_COUNT);
    }
}
