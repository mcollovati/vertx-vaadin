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
package com.github.mcollovati.vertx.vaadin.quarkus.it.push;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("push")
public class PushUpdateDivView extends Div {
    private final AtomicInteger count = new AtomicInteger();

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    private static final int DELAY = 100;

    private static final int MAX_UPDATE = 50;

    public PushUpdateDivView() {
        setId("push-update");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        updateDiv();
        scheduleUpdate(attachEvent.getUI());
    }

    private void scheduleUpdate(final UI ui) {
        service.schedule(
                () -> {
                    ui.access(this::updateDiv);
                    if (count.getAndIncrement() < MAX_UPDATE) {
                        scheduleUpdate(ui);
                    } else {
                        service.shutdown();
                    }
                },
                DELAY,
                TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        service.shutdownNow();
    }

    private void updateDiv() {
        setText(String.valueOf(count.get()));
    }
}
