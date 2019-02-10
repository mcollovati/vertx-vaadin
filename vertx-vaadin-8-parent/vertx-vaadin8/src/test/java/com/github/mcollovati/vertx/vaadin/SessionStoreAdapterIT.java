/*
 * The MIT License
 * Copyright © 2016-2019 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by marco on 16/07/16.
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class SessionStoreAdapterIT {

    private static final int PORT = 8080;
    Vertx vertx;
    SessionTestVerticle testVerticle;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        testVerticle = new SessionTestVerticle();
        vertx.deployVerticle(testVerticle, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testVerticle(TestContext context) {

        final Async async = context.async();

        vertx.createHttpClient().getNow(PORT, "localhost", "/",
            response -> response.handler(b -> {
                context.assertTrue(b.toString().contains("Done"));
                async.complete();
            })
        );
    }

    @VaadinServletConfiguration(productionMode = false, ui = MyUi.class)
    public static class SessionTestVerticle extends VaadinVerticle {

        List<String> sessionEvents = new ArrayList<>();

        void registerSessionEvent(String event) {
            sessionEvents.add(event);
        }

        @Override
        protected void serviceInitialized(VertxVaadinService service, Router router) {
            context.put("mySelf", this);
            router.get("/destroySession").handler(rc -> {
                rc.session().destroy();
                rc.response().end("Session destroyed");
            });
        }

    }

    public static class MyUi extends UI {

        @Override
        protected void init(VaadinRequest request) {
            request.getService().addSessionDestroyListener(e -> {
                ((SessionTestVerticle)((VertxVaadinService) e.getService()).getVertx().getOrCreateContext().get("mySelf"))
                    .registerSessionEvent("Session destroyed");
            });
        }
    }

}
