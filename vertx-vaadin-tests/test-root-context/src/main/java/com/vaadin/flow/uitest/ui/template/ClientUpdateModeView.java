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
package com.vaadin.flow.uitest.ui.template;

import java.util.stream.Stream;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.ClientUpdateMode;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.AbstractDivView;

@Route("com.vaadin.flow.uitest.ui.template.ClientUpdateModeView")
public class ClientUpdateModeView extends AbstractDivView {

    public interface ClientUpdateModeModel extends TemplateModel {
        public String getValue();

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getIndirectAllowed();

        public String getIndirect();

        @AllowClientUpdates(ClientUpdateMode.DENY)
        public String getTwoWayDenied();
    }

    @Tag("client-update-mode")
    @JsModule("ClientUpdateMode.js")
    public static class ClientUpdateModeTemplate extends PolymerTemplate<ClientUpdateModeModel> {}

    public ClientUpdateModeView() {
        ClientUpdateModeTemplate template = new ClientUpdateModeTemplate();
        add(template);

        Element element = template.getElement();
        Stream.of("value", "indirectAllowed", "indirect", "twoWayDenied").forEach(propertyName -> {
            element.addPropertyChangeListener(
                    propertyName,
                    event -> add(new Text(propertyName + " changed to " + event.getValue()), new Html("<br>")));
        });
    }
}
