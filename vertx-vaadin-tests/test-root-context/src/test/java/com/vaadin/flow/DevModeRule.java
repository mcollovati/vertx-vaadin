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
package com.vaadin.flow;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DevModeRule implements TestRule {

    private final String productionModeProperty;

    public DevModeRule() {
        this("vaadin.productionMode");
    }

    public DevModeRule(String productionModeProperty) {
        this.productionModeProperty = productionModeProperty;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        boolean ignoreOnDevMode = description.getAnnotation(DevModeOnly.class) != null
                || description.getTestClass().getAnnotation(DevModeOnly.class) != null;
        boolean isProductionMode = Boolean.getBoolean(productionModeProperty)
                || (System.getProperties().contains(productionModeProperty)
                        && System.getProperty(productionModeProperty) == null);
        if (isProductionMode && ignoreOnDevMode) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    throw new AssumptionViolatedException(
                            description.getDisplayName() + " can run only on development mode");
                }
            };
        }
        return base;
    }
}
