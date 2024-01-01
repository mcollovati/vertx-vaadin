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
package com.vaadin.flow.uitest.ui.dependencies;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("com.vaadin.flow.uitest.ui.dependencies.DynamicDependencyView")
public class DynamicDependencyView extends Div {
    private final Div newComponent = new Div();

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            newComponent.setId("new-component");
            add(newComponent);

            attachEvent
                    .getUI()
                    .getPage()
                    .addDynamicImport("return new Promise( "
                            + " function( resolve, reject){ "
                            + "   var div = document.createElement(\"div\");\n"
                            + "     div.setAttribute('id','dep');\n"
                            + "     div.textContent = document.querySelector('#new-component')==null;\n"
                            + "     document.body.appendChild(div);resolve('');}"
                            + ");");

            add(createLoadButton(
                    "nopromise",
                    "Load non-promise dependency",
                    "document.querySelector('#new-component').textContent = 'import has been run'"));
            add(createLoadButton("throw", "Load throwing dependency", "throw Error('Throw on purpose')"));
            add(createLoadButton(
                    "reject",
                    "Load rejecting dependency",
                    "return new Promise(function(resolve, reject) { reject(Error('Reject on purpose')); });"));
        }
    }

    private NativeButton createLoadButton(String id, String name, String expression) {
        NativeButton button = new NativeButton(name, event -> {
            UI.getCurrent().getPage().addDynamicImport(expression);
            newComponent.setText("Div updated for " + id);
        });
        button.setId(id);
        return button;
    }
}
