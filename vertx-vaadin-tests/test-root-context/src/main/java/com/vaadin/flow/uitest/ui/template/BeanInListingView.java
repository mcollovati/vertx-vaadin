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

import java.util.Arrays;
import java.util.List;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.template.BeanInListingView.ListModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.BeanInListingView", layout = ViewTestLayout.class)
@Tag("listing-bean-view")
@JsModule("BeanInListing.js")
public class BeanInListingView extends PolymerTemplate<ListModel> {

    public interface ListModel extends TemplateModel {

        void setUsers(List<User> users);

        @AllowClientUpdates
        List<User> getUsers();

        @AllowClientUpdates
        User getActiveUser();

        void setActiveUser(User user);

        void setSelected(String name);

        void setMessages(List<String> messages);

        @AllowClientUpdates
        String getActiveMessage();
    }

    public static class User {
        private String name;

        @AllowClientUpdates
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public BeanInListingView() {
        setId("template");
        getModel().setUsers(Arrays.asList(createUser("foo"), createUser("bar")));

        getModel().setMessages(Arrays.asList("baz", "msg"));

        getElement().addPropertyChangeListener("activeUser", event -> patientSelected());

        getElement().addPropertyChangeListener("activeMessage", event -> messageSelected());

        getModel().setActiveUser(getModel().getUsers().get(0));
    }

    private User createUser(String name) {
        User patient = new User();
        patient.setName(name);
        return patient;
    }

    private void patientSelected() {
        User user = getModel().getActiveUser();
        getModel().setSelected(user.getName());
    }

    private void messageSelected() {
        String msg = getModel().getActiveMessage();
        getModel().setSelected(msg);
    }
}
