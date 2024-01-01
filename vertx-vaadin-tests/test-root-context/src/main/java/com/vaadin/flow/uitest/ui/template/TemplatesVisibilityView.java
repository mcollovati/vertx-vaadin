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

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateNode;
import com.vaadin.flow.internal.nodefeature.VirtualChildrenList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.TemplatesVisibilityView", layout = ViewTestLayout.class)
public class TemplatesVisibilityView extends AbstractDivView {

    public TemplatesVisibilityView() {
        JsGrandParentView grandParent = new JsGrandParentView();
        grandParent.setId("grand-parent");

        grandParent.setVisible(false);

        add(grandParent);

        add(createButton(
                "Change grand parent visibility",
                "grand-parent-visibility",
                event -> grandParent.setVisible(!grandParent.isVisible())));

        StateNode subTemplateChild = grandParent
                .getElement()
                .getNode()
                .getFeature(VirtualChildrenList.class)
                .iterator()
                .next();

        JsSubTemplate subTemplate =
                (JsSubTemplate) Element.get(subTemplateChild).getComponent().get();

        add(createButton(
                "Change sub template visibility",
                "sub-template-visibility",
                event -> subTemplate.setVisible(!subTemplate.isVisible())));

        StateNode grandChildNode = subTemplate
                .getElement()
                .getNode()
                .getFeature(VirtualChildrenList.class)
                .iterator()
                .next();

        JsInjectedGrandChild grandChild = (JsInjectedGrandChild)
                Element.get(grandChildNode).getComponent().get();

        add(createButton(
                "Change grand child visibility",
                "grand-child-visibility",
                event -> grandChild.setVisible(!grandChild.isVisible())));

        add(createButton(
                "Update sub template property via client side",
                "client-side-update-property",
                event -> grandParent.updateChildViaClientSide()));
    }
}
