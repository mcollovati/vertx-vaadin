/*
 * The MIT License
 * Copyright © 2016-2020 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.connect.rest;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import dev.hilla.Endpoint;

/**
 * Source taken from Vaadin Flow (https://github.com/vaadin/flow)
 */
@Endpoint
public class VaadinConnectEndpoints {

    public BeanWithZonedDateTimeField getBeanWithZonedDateTimeField() {
        return new BeanWithZonedDateTimeField();
    }

    public BeanWithPrivateFields getBeanWithPrivateFields() {
        return new BeanWithPrivateFields();
    }

    public BeanWithJacksonAnnotation getBeanWithJacksonAnnotation() {
        return new BeanWithJacksonAnnotation();
    }

    public LocalTime getLocalTime() {
        return LocalTime.of(8, 0, 0);
    }

    public static class BeanWithZonedDateTimeField {
        private ZonedDateTime zonedDateTime = ZonedDateTime.now();

        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }
    }
}
