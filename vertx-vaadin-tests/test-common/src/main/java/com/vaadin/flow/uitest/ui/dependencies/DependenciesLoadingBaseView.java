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
package com.vaadin.flow.uitest.ui.dependencies;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.uitest.ui.AbstractDivView;

/**
 * This class provides test base for IT test that check dependencies being
 * loaded correctly.
 *
 * @author Vaadin Ltd
 * @since 1.0.
 */
public class DependenciesLoadingBaseView extends AbstractDivView {
    static final String PRELOADED_DIV_ID = "preloadedDiv";
    static final String INLINE_CSS_TEST_DIV_ID = "inlineCssTestDiv";
    static final String DOM_CHANGE_TEXT = "I appear after inline and eager dependencies and before lazy";

    protected DependenciesLoadingBaseView(String cssSuffix) {
        add(
                createDiv(PRELOADED_DIV_ID + cssSuffix, "Preloaded div"),
                createDiv(INLINE_CSS_TEST_DIV_ID + cssSuffix, "A div for testing inline css"));
    }

    private Div createDiv(String id, String text) {
        Div div = new Div();
        div.setId(id);
        div.setText(text);
        return div;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // See eager.js for attachTestDiv code
        getPage().executeJs("window.attachTestDiv($0)", DOM_CHANGE_TEXT);
    }
}
