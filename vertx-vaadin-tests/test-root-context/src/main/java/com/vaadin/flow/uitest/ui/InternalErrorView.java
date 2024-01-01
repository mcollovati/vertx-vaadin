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

import java.io.IOException;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.CustomizedSystemMessages;
import com.vaadin.flow.server.DefaultSystemMessagesProvider;
import com.vaadin.flow.server.SystemMessages;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.JsonConstants;

/**
 * @author Vaadin Ltd
 * @since 1.0.
 */
@Route("com.vaadin.flow.uitest.ui.InternalErrorView")
public class InternalErrorView extends AbstractDivView {

    public InternalErrorView() {
        Div message = new Div();
        message.setId("message");

        NativeButton updateMessageButton = createButton("Update", "update", event -> message.setText("Updated"));

        NativeButton closeSessionButton =
                createButton("Close session", "close-session", event -> VaadinSession.getCurrent()
                        .close());

        NativeButton enableNotificationButton = createButton(
                "Enable session expired notification",
                "enable-notification",
                event -> enableSessionExpiredNotification());

        NativeButton causeExceptionButton =
                createButton("Cause exception", "cause-exception", event -> showInternalError());

        NativeButton resetSystemMessagesButton =
                createButton("Reset system messages", "reset-system-messages", event -> resetSystemMessages());

        add(
                message,
                updateMessageButton,
                closeSessionButton,
                enableNotificationButton,
                causeExceptionButton,
                resetSystemMessagesButton);
    }

    private void showInternalError() {
        SystemMessages systemMessages =
                VaadinService.getCurrent().getSystemMessages(getLocale(), VaadinRequest.getCurrent());

        showCriticalNotification(
                systemMessages.getInternalErrorCaption(),
                systemMessages.getInternalErrorMessage(),
                "",
                systemMessages.getInternalErrorURL());
    }

    protected void showCriticalNotification(String caption, String message, String details, String url) {
        VaadinService service = VaadinService.getCurrent();
        VaadinResponse response = VaadinService.getCurrentResponse();

        try {
            service.writeUncachedStringResponse(
                    response,
                    JsonConstants.JSON_CONTENT_TYPE,
                    VaadinService.createCriticalNotificationJSON(caption, message, details, url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableSessionExpiredNotification() {
        CustomizedSystemMessages sysMessages = new CustomizedSystemMessages();
        sysMessages.setSessionExpiredNotificationEnabled(true);

        VaadinService.getCurrent().setSystemMessagesProvider(systemMessagesInfo -> sysMessages);
    }

    private void resetSystemMessages() {
        VaadinService.getCurrent().setSystemMessagesProvider(DefaultSystemMessagesProvider.get());
    }
}
