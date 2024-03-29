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
package com.vaadin.flow.uitest.ui.template.collections;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.ModelItem;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.AllowClientUpdates;
import com.vaadin.flow.templatemodel.ClientUpdateMode;
import com.vaadin.flow.templatemodel.TemplateModel;
import com.vaadin.flow.uitest.ui.template.collections.ListBindingView.ListBindingModel;
import com.vaadin.flow.uitest.vertx.ViewTestLayout;

@Route(value = "com.vaadin.flow.uitest.ui.template.collections.ListBindingView", layout = ViewTestLayout.class)
@Tag("list-binding")
@JsModule("ListBinding.js")
public class ListBindingView extends PolymerTemplate<ListBindingModel> {
    static final List<String> RESET_STATE = Arrays.asList("1", "2", "3");
    static final String INITIAL_STATE = "foo";

    public interface ListBindingModel extends TemplateModel {
        void setSelectedMessage(Message selectedMessage);

        void setMessages(List<Message> messages);

        @AllowClientUpdates(ClientUpdateMode.ALLOW)
        List<Message> getMessages();
    }

    public ListBindingView() {
        setId("template");
        getModel().setMessages(Collections.singletonList(new Message(INITIAL_STATE)));
    }

    @EventHandler
    private void reset() {
        getModel().setMessages(RESET_STATE.stream().map(Message::new).collect(Collectors.toList()));
    }

    @EventHandler
    private void selectItem(@ModelItem Message message) {
        getModel().setSelectedMessage(message);
    }

    @EventHandler
    private void addElement() {
        getModel().getMessages().add(new Message("4"));
    }

    @EventHandler
    private void addElementByIndex() {
        getModel().getMessages().add(0, new Message("4"));
    }

    @EventHandler
    private void addNumerousElements() {
        List<Message> newMessages = Arrays.asList(new Message("4"), new Message("5"));
        getModel().getMessages().addAll(newMessages);
    }

    @EventHandler
    private void addNumerousElementsByIndex() {
        List<Message> newMessages = Arrays.asList(new Message("4"), new Message("5"));
        getModel().getMessages().addAll(0, newMessages);
    }

    @EventHandler
    private void clearList() {
        getModel().getMessages().clear();
    }

    @EventHandler
    private void removeSecondElementByIndex() {
        List<Message> currentMessages = getModel().getMessages();
        if (currentMessages.size() > 2) {
            currentMessages.remove(1);
        }
    }

    @EventHandler
    private void removeFirstElementWithIterator() {
        if (!getModel().getMessages().isEmpty()) {
            Iterator<Message> iterator = getModel().getMessages().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    @EventHandler
    private void swapFirstAndSecond() {
        List<Message> messages = getModel().getMessages();
        if (messages.size() > 1) {
            Message first = messages.get(0);
            messages.set(0, messages.get(1));
            messages.set(1, first);
        }
    }

    @EventHandler
    private void sortDescending() {
        getModel().getMessages().sort(Comparator.comparing(Message::getText).reversed());
    }

    @EventHandler
    private void setInitialStateToEachMessage() {
        getModel().getMessages().forEach(message -> message.setText(INITIAL_STATE));
    }
}
