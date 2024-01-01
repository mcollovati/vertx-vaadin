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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

/**
 * Test for https://github.com/vaadin/flow/issues/7590
 */
@Route("com.vaadin.flow.uitest.ui.ResynchronizationView")
public class ResynchronizationView extends AbstractDivView {
    static final String ID = "ResynchronizationView";

    static final String ADD_BUTTON = "add";
    static final String CALL_BUTTON = "call";

    static final String ADDED_CLASS = "added";

    public ResynchronizationView() {
        setId(ID);

        add(createButton("Desync and add", ADD_BUTTON, e -> {
            final Span added = new Span("added");
            added.addClassName(ADDED_CLASS);
            add(added);
            triggerResync();
        }));
    }

    private void triggerResync() {
        // trigger a resynchronization request on the client
        getUI().get().getInternals().incrementServerId();
    }
}
