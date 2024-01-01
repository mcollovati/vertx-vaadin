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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.ClientUpdateMode;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.AbstractDivView;

@Route("com.vaadin.flow.uitest.ui.template.EmptyListsView")
public class EmptyListsView extends AbstractDivView {

    @JsModule("EmptyLists.js")
    @com.vaadin.flow.component.Tag("empty-list")
    public static class EmptyLists extends PolymerTemplate<EmptyListsModel> {
        public EmptyLists() {
            Item item = new Item();
            item.setLabel("foo");
            getModel().setItems(Collections.singletonList(item));
        }

        @Override
        protected EmptyListsModel getModel() {
            return super.getModel();
        }
    }

    public static class Tag {
        private String name;

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Item {

        private String label;

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public List<Tag> getTags() {
            return new ArrayList<>();
        }

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public interface EmptyListsModel extends TemplateModel {
        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        List<Item> getItems();

        void setItems(List<Item> items);
    }

    public EmptyListsView() {
        EmptyLists template = new EmptyLists();
        template.setId("template");
        add(template);

        add(createButton("Set an empty list of items", "set-empty", event -> template.getModel()
                .setItems(new ArrayList<>())));
    }
}
