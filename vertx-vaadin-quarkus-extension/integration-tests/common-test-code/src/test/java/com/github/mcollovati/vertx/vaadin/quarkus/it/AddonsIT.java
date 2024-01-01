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
package com.github.mcollovati.vertx.vaadin.quarkus.it;

import com.vaadin.testbench.TestBenchElement;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class AddonsIT extends AbstractChromeIT {

    @Override
    protected String getTestPath() {
        return "/addons";
    }

    @Test
    void addonWithoutJandexIndexedOnApplicationPropertiesShouldBeRendered() {
        open();
        TestBenchElement addon = waitUntil(driver -> $("hello-world").first());
        Assertions.assertTrue(
                addon.$("span").attribute("id", "without-jandex").exists(),
                "Expecting span element with id 'without-jandex' to be present in 'hello-world' shadow DOM, but was not");
        Assertions.assertTrue(
                addon.$("axa-input-text")
                        .attribute("id", "npm-dep")
                        .first()
                        .$("input")
                        .first()
                        .hasClassName("a-input-text__input"),
                "Expecting axa-input-text element with id 'npm-dep' to be rendered in 'hello-world' shadow DOM, but was not");
    }

    @Test
    void addonWithJandexIndexedShouldBeRendered() {
        open();
        TestBenchElement addon = waitUntil(driver -> $("hello-world-jandex").first());
        Assertions.assertTrue(
                addon.$("span").attribute("id", "with-jandex").exists(),
                "Expecting span element with id 'with-jandex' to be present in 'hello-world-jandex' shadow DOM, but was not");
        Assertions.assertTrue(
                addon.$("axa-input-text")
                        .attribute("id", "npm-dep-jandex")
                        .first()
                        .$("input")
                        .first()
                        .hasClassName("a-input-text__input"),
                "Expecting axa-input-text element with id 'npm-dep-jandex' to be rendered in 'hello-world-jandex' shadow DOM, but was not");
    }
}
