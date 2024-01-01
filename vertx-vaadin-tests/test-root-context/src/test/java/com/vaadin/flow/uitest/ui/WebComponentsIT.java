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
package com.vaadin.flow.uitest.ui;

import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

import com.vaadin.flow.testutil.ChromeBrowserTest;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

public class WebComponentsIT extends ChromeBrowserTest {

    @Test
    public void testPolyfillLoaded() {
        open();

        LogEntries logs = driver.manage().logs().get("browser");
        if (logs != null) {
            Optional<LogEntry> anyError = StreamSupport.stream(logs.spliterator(), true)
                    .filter(entry -> entry.getLevel().intValue() > Level.INFO.intValue())
                    .filter(entry -> !entry.getMessage().contains("favicon.ico"))
                    .filter(entry -> !entry.getMessage().contains("sockjs-node"))
                    .filter(entry -> !entry.getMessage().contains("[WDS] Disconnected!"))
                    .filter(entry -> !entry.getMessage().contains("Lit is in dev mode. Not recommended for production"))
                    .findAny();
            anyError.ifPresent(entry -> Assert.fail(entry.getMessage()));
        }
    }
}
