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
package com.vaadin.viteapp.views.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import org.vaadin.example.addon.AddonLitComponent;

@Route(TemplateView.ROUTE)
public class TemplateView extends Div {

    public static final String ROUTE = "template";

    public TemplateView() {
        LitComponent litComponent = new LitComponent();
        add(litComponent);

        PolymerComponent polymerComponent = new PolymerComponent();
        add(polymerComponent);

        AddonLitComponent addonLitComponent = new AddonLitComponent();
        add(addonLitComponent);

        Input setLabelInput = new Input();
        add(setLabelInput);

        NativeButton setLabelButton = new NativeButton("Set labels");
        setLabelButton.addClickListener(e -> {
            String newLabel = setLabelInput.getValue();
            litComponent.setLabel(newLabel);
            polymerComponent.setLabel(newLabel);
            addonLitComponent.setLabel(newLabel);
        });
        add(setLabelButton);

        // Add component by reflection to excercise fallback chunk
        try {
            Class<?> clazz = Class.forName("com.vaadin.viteapp.views.template.ReflectivelyReferencedComponent");
            add((Component) clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
