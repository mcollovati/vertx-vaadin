/*
 * The MIT License
 * Copyright © 2000-2021 Marco Collovati (mcollovati@gmail.com)
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

import java.util.logging.Logger;

import com.vaadin.testbench.ScreenshotOnFailureRule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.runner.Description;
import org.mockito.Mockito;

public class ScreenshotsOnFailureExtension implements TestExecutionExceptionHandler {

    private static class ScreenshotOnFailureRuleDelegate extends ScreenshotOnFailureRule {
        public ScreenshotOnFailureRuleDelegate(AbstractChromeIT test) {
            super(test);
        }

        @Override
        protected void failed(Throwable throwable, Description description) {
            super.failed(throwable, description);
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (!context.getTestInstance().isPresent()) {
            getLogger().warning("There is no test instance in the context, can't generate a screenshot");
            throw throwable;
        }
        Object object = context.getTestInstance().get();
        AbstractChromeIT test = (AbstractChromeIT) object;

        ScreenshotOnFailureRuleDelegate delegate = new ScreenshotOnFailureRuleDelegate(test);
        if (!context.getTestClass().isPresent()) {
            getLogger().warning("There is no test class in the context, can't generate a screenshot");
            throw throwable;
        }

        Description description;
        if (context.getTestMethod().isPresent()) {
            description = Description.createTestDescription(
                    context.getTestClass().get(), context.getTestMethod().get().getName());
        } else {
            description = Mockito.mock(Description.class);
            Mockito.when(description.getDisplayName()).thenReturn(context.getDisplayName());
        }
        delegate.failed(throwable, description);
        throw throwable;
    }

    private Logger getLogger() {
        return Logger.getLogger(ScreenshotsOnFailureExtension.class.getName());
    }
}
