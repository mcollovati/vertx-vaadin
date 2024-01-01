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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.AbstractDivView;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.TemplateMappingDetectorView", layout = ViewTestLayout.class)
public class TemplateMappingDetectorView extends AbstractDivView {

    @Tag("div")
    public static class TemplateMappingDetector extends Component implements HasText {

        public TemplateMappingDetector() {
            setText("Template mapped: " + isTemplateMapped());
        }
    }

    public static class TemplateMappingDetectorComposite extends Composite<TemplateMappingDetector> {

        @Override
        protected TemplateMappingDetector initContent() {
            TemplateMappingDetector detector = super.initContent();
            detector.setText("Composite template mapped: " + isTemplateMapped() + " " + detector.getText());
            return detector;
        }
    }

    @Tag("template-mapping-detector")
    @JsModule("TemplateMappingDetector.js")
    public static class TemplateMappingDetectorContainer extends PolymerTemplate<TemplateModel> {
        @Id
        TemplateMappingDetector detector1;

        // Disabled due to https://github.com/vaadin/flow/issues/3104

        // @Id
        // TemplateMappingDetectorComposite detector2;

        @Id
        Div container;

        public TemplateMappingDetectorContainer() {
            TemplateMappingDetector detector3 = new TemplateMappingDetector();
            detector3.setId("detector2");
            TemplateMappingDetectorComposite detector4 = new TemplateMappingDetectorComposite();
            detector4.setId("detector3");
            Div detector5 = new Div();
            detector5.setId("detector4");
            detector5.setText("The template itself: " + isTemplateMapped());
            container.add(detector3, detector4, detector5);
        }
    }

    @Tag("template-mapping-detector-parent")
    @JsModule("TemplateMappingDetectorParent.js")
    public static class TemplateMappingDetectorContainerParent extends PolymerTemplate<TemplateModel> {
        @Id
        TemplateMappingDetectorContainer detector;
    }

    public static class TemplateMappingDetectorContainerComposite extends Composite<TemplateMappingDetectorContainer> {}

    @Override
    protected void onShow() {
        TemplateMappingDetectorContainer container = new TemplateMappingDetectorContainer();
        TemplateMappingDetectorContainerParent containerParent = new TemplateMappingDetectorContainerParent();
        TemplateMappingDetectorContainerComposite composite = new TemplateMappingDetectorContainerComposite();
        composite.setId("composite");
        add(container, new Hr(), containerParent, new Hr(), composite);
    }
}
