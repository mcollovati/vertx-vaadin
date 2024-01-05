/*
 * The MIT License
 * Copyright © 2000-2022 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.theme.lumo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.theme.AbstractTheme;
import com.vaadin.flow.uitest.ui.dependencies.TestVersion;
import org.slf4j.LoggerFactory;

/**
 * Lumo component theme class implementation. Used for testing theming features.
 *
 * @since 1.0
 */
@NpmPackage(value = "@vaadin/vaadin-themable-mixin", version = TestVersion.VAADIN)
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = TestVersion.VAADIN)
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/vaadin-lumo-styles", version = TestVersion.VAADIN)
@JsModule("@vaadin/vaadin-lumo-styles/color.js")
@JsModule("@vaadin/vaadin-lumo-styles/typography.js")
@JsModule("@vaadin/vaadin-lumo-styles/sizing.js")
@JsModule("@vaadin/vaadin-lumo-styles/spacing.js")
@JsModule("@vaadin/vaadin-lumo-styles/style.js")
@JsModule("@vaadin/vaadin-lumo-styles/vaadin-iconset.js")
//@JsModule("./lumo-includes.ts")
public class Lumo implements AbstractTheme {

    public static final String LIGHT = "light";
    public static final String DARK = "dark";

    @Override
    public String getBaseUrl() {
        return "src/";
    }

    @Override
    public String getThemeUrl() {
        return "theme/lumo/";
    }

    @Override
    public List<String> getHeaderInlineContents() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getHtmlAttributes(String variant) {
        if (variant.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> attributes = new HashMap<>(1);
        switch (variant) {
            case LIGHT:
                attributes.put("theme", LIGHT);
                break;
            case DARK:
                attributes.put("theme", DARK);
                break;
            default:
                LoggerFactory.getLogger(Lumo.class.getName())
                        .warn("Lumo theme variant not recognized: '{}'. Using no variant.", variant);
        }
        return attributes;
    }
}
