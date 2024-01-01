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

import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.ui.LoadMode;

/**
 * See corresponding IT for more details.
 *
 * @author Vaadin Ltd
 * @since 1.0.
 */
@Route("com.vaadin.flow.uitest.ui.dependencies.DependenciesLoadingAnnotationsView")
@JavaScript(value = "./dependencies/inline.js", loadMode = LoadMode.INLINE)
@StyleSheet(value = "./dependencies/inline.css", loadMode = LoadMode.INLINE)
@JavaScript(value = "./dependencies/lazy.js", loadMode = LoadMode.LAZY)
@StyleSheet(value = "./dependencies/lazy.css", loadMode = LoadMode.LAZY)
@JavaScript("./dependencies/eager.js")
@StyleSheet("./dependencies/eager.css")
@JsModule("./dependencies/eager-module.js")
public class DependenciesLoadingAnnotationsView extends DependenciesLoadingBaseView {

    public DependenciesLoadingAnnotationsView() {
        super("");
    }
}
