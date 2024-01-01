/*
 * The MIT License
 * Copyright © 2000-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.vaadin.flow.TestPush;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.ui.Transport;

/**
 * Test for reproducing the bug https://github.com/vaadin/flow/issues/4660
 *
 * @since 1.4
 */
@Route("com.vaadin.flow.uitest.ui.LongPollingMultipleThreadsView")
@TestPush(transport = Transport.LONG_POLLING)
public class LongPollingMultipleThreadsView extends AbstractDivView {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    private final List<Long> itemRegistry = new LinkedList<>();

    private final Span label = new Span();

    private UI ui;

    public LongPollingMultipleThreadsView() {
        this.setId("push-update");
        NativeButton startButton = createButton("start", "start-button", event -> this.start());
        add(startButton, label);
    }

    private void start() {
        synchronized (itemRegistry) {
            itemRegistry.clear();
        }

        updateDiv(new ArrayList<>());

        for (int i = 0; i < 5; ++i) {
            executor.submit(this::doWork);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        updateDiv(new ArrayList<>());
        ui = attachEvent.getUI();
    }

    private void doWork() {
        try {
            Thread.sleep((int) (Math.random() * 10));
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // Simulate some new piece of data coming in on a background thread and
        // getting put into the display.
        List<Long> copy;
        synchronized (itemRegistry) {
            itemRegistry.add(System.currentTimeMillis());

            // Copy the list so we're not accessing the shared registry
            // concurrently.
            copy = new ArrayList<>(itemRegistry);
        }

        ui.access(() -> updateDiv(copy));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        executor.shutdownNow();
    }

    private void updateDiv(List<Long> items) {
        label.setText(items.toString());
    }
}
