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
package com.vaadin.flow.uitest.ui.webcomponent;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;

/**
 * Defines setters and getters for a "max" property with a default of 100.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public interface HasMax extends HasElement {

    class Descriptors {
        // Hides descriptors
        static final PropertyDescriptor<Integer, Integer> maxProperty =
                PropertyDescriptors.propertyWithDefault("max", 100);
    }

    /**
     * Sets the max value.
     *
     * @param max
     *            the max value to use
     */
    default void setMax(int max) {
        Descriptors.maxProperty.set(this, max);
    }

    /**
     * Gets the max value.
     *
     * @return the max value
     */
    default int getMax() {
        return Descriptors.maxProperty.get(this);
    }
}
