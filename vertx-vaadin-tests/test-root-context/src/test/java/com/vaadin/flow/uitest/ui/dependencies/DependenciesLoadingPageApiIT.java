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

import org.junit.Ignore;

/**
 * See {@link DependenciesLoadingAnnotationsIT} for more details about the test.
 * Test runs and performs the same checks as
 * {@link DependenciesLoadingAnnotationsIT}, but this test opens a different
 * page to test, that's why the class exists and needed.
 *
 * @author Vaadin Ltd
 * @see DependenciesLoadingAnnotationsIT
 * @since 1.0.
 */
@Ignore("Doesn't work ccdm, see https://github.com/vaadin/flow/issues/7328")
public class DependenciesLoadingPageApiIT extends DependenciesLoadingAnnotationsIT {

    @Override
    protected String getCssSuffix() {
        return "WebRes";
    }
}
