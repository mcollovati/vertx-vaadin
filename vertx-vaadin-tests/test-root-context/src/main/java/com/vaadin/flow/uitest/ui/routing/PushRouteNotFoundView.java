/*
 * The MIT License
 * Copyright © 2000-2018 Marco Collovati (mcollovati@gmail.com)
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
package com.vaadin.flow.uitest.ui.routing;

import jakarta.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouteNotFoundError;

@ParentLayout(PushLayout.class)
public class PushRouteNotFoundView extends RouteNotFoundError {

    public static String PUSH_NON_EXISTENT_PATH = "push-no-route";

    private boolean isPushPath;

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        String path = event.getLocation().getPath();
        if (PUSH_NON_EXISTENT_PATH.equals(path)) {
            isPushPath = true;
            return HttpServletResponse.SC_NOT_FOUND;
        } else {
            return super.setErrorParameter(event, parameter);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (isPushPath) {
            Element div = ElementFactory.createDiv(
                    "Push mode: " + attachEvent.getUI().getPushConfiguration().getPushMode());
            div.setAttribute("id", "push-mode");
            getElement().appendChild(div);
        }
    }
}
