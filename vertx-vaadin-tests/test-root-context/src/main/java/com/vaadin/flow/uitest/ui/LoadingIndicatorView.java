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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.page.LoadingIndicatorConfiguration;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.LoadingIndicatorView", layout = ViewTestLayout.class)
public class LoadingIndicatorView extends AbstractDivView {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        NativeButton disableButton = new NativeButton("Disable default loading indicator theme and add custom");
        disableButton.setId("disable-theme");
        disableButton.addClickListener(clickEvent -> {
            clickEvent
                    .getSource()
                    .getUI()
                    .get()
                    .getLoadingIndicatorConfiguration()
                    .setApplyDefaultTheme(false);
            clickEvent.getSource().getUI().get().getPage().addStyleSheet("/view/loading-indicator.css");
        });
        add(disableButton);
        add(divWithText("First delay: " + getLoadingIndicatorConfiguration().getFirstDelay()));
        add(divWithText("Second delay: " + getLoadingIndicatorConfiguration().getSecondDelay()));
        add(divWithText("Third delay: " + getLoadingIndicatorConfiguration().getThirdDelay()));

        int[] delays = new int[] {100, 200, 500, 1000, 2000, 5000, 10000};
        for (int delay : delays) {
            add(createButton("Trigger event which takes " + delay + "ms", "wait" + delay, e -> delay(delay)));
        }
    }

    private static Div divWithText(String text) {
        Div div = new Div();
        div.setText(text);
        return div;
    }

    private LoadingIndicatorConfiguration getLoadingIndicatorConfiguration() {
        return UI.getCurrent().getLoadingIndicatorConfiguration();
    }

    private void delay(int timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
        }
    }
}
