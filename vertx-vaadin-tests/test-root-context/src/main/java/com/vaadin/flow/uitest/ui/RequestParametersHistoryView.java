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

import java.util.List;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd
 * @since 1.0.
 */
@Route("com.vaadin.flow.uitest.ui.RequestParametersHistoryView")
public class RequestParametersHistoryView extends AbstractDivView implements AfterNavigationObserver {

    static final String REQUEST_PARAM_NAME = "testRequestParam";
    static final String NO_INPUT_TEXT = "No input";
    static final String REQUEST_PARAM_ID = "requestParamDisplayLabel";
    static final String BACK_BUTTON_ID = "backButton";

    private final Label requestParamLabel;

    public RequestParametersHistoryView() {
        NativeButton backwardButton = createButton(
                "Go back", BACK_BUTTON_ID, event -> getPage().getHistory().back());
        requestParamLabel = new Label(NO_INPUT_TEXT);
        requestParamLabel.setId(REQUEST_PARAM_ID);
        add(requestParamLabel, backwardButton);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        List<String> params =
                event.getLocation().getQueryParameters().getParameters().get(REQUEST_PARAM_NAME);
        if (params == null || params.isEmpty()) {
            requestParamLabel.setText(NO_INPUT_TEXT);
        } else {
            requestParamLabel.setText(params.get(0));
        }
    }
}
