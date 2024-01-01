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
package com.github.mcollovati.vertx.vaadin;

import java.util.Collections;
import java.util.Enumeration;

import com.vaadin.flow.server.VaadinConfig;
import com.vaadin.flow.server.VaadinContext;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class VertxVaadinConfig implements VaadinConfig {

    private final JsonObject config;
    private final VaadinContext vaadinContext;

    public VertxVaadinConfig(JsonObject config, VaadinContext vaadinContext) {
        this.config = config;
        this.vaadinContext = vaadinContext;
    }

    @Override
    public VaadinContext getVaadinContext() {
        return vaadinContext;
    }

    @Override
    public Enumeration<String> getConfigParameterNames() {
        return Collections.enumeration(config.fieldNames());
    }

    @Override
    public String getConfigParameter(String name) {
        Object value = config.getValue(name);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return Json.encode(value);
        }
        return null;
    }
}
