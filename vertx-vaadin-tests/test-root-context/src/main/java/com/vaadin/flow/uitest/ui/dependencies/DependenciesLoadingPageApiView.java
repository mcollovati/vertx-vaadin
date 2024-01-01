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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.ui.LoadMode;

/**
 * See {@link DependenciesLoadingAnnotationsView} for more details about the
 * test.
 *
 * @author Vaadin Ltd
 * @since 1.0.
 * @see DependenciesLoadingAnnotationsView
 */
@Route("com.vaadin.flow.uitest.ui.dependencies.DependenciesLoadingPageApiView")
public class DependenciesLoadingPageApiView extends DependenciesLoadingBaseView {

    public DependenciesLoadingPageApiView() {
        super("WebRes");
        Page page = UI.getCurrent().getPage();
        page.addJavaScript("/dependencies/inline.js", LoadMode.INLINE);
        page.addStyleSheet("/dependencies/inline.css", LoadMode.INLINE);
        page.addJavaScript("/dependencies/lazy.js", LoadMode.LAZY);
        page.addStyleSheet("/dependencies/lazy.css", LoadMode.LAZY);
        page.addJavaScript("/dependencies/eager.js");
        page.addStyleSheet("/dependencies/eager.css");
        page.addJsModule("/dependencies/eager-module.js");
    }
}
