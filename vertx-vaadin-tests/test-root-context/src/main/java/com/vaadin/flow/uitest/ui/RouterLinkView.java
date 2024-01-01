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
package com.vaadin.flow.uitest.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;

@Route("com.vaadin.flow.uitest.ui.RouterLinkView")
public class RouterLinkView extends AbstractDivView {

    public RouterLinkView() {
        Element bodyElement = getElement();
        bodyElement.getStyle().set("margin", "1em");

        Element location = ElementFactory.createDiv("no location").setAttribute("id", "location");

        Element queryParams = ElementFactory.createDiv("no queryParams").setAttribute("id", "queryParams");

        bodyElement.appendChild(location, new Element("p"));
        bodyElement.appendChild(queryParams, new Element("p"));

        addLinks();

        getPage().getHistory().setHistoryStateChangeHandler(e -> {
            location.setText(e.getLocation().getPath());
            queryParams.setText(e.getLocation().getQueryParameters().getQueryString());
            if (e.getState().isPresent())
                UI.getCurrent()
                        .getPage()
                        .getHistory()
                        .pushState(null, ((JsonObject) e.getState().get()).getString("href"));
        });

        addImageLink();
    }

    private void addImageLink() {
        Anchor anchor = new Anchor("image/link", (String) null);
        anchor.getElement().setAttribute("router-link", true);
        anchor.getStyle().set("display", "block");

        Image image = new Image("", "IMAGE");
        image.setWidth("200px");
        image.setHeight("200px");

        anchor.add(image);
        add(anchor);
    }

    protected void addLinks() {
        getElement()
                .appendChild(
                        // inside servlet mapping
                        ElementFactory.createDiv("inside this servlet"),
                        ElementFactory.createRouterLink("", "empty"),
                        new Element("p"),
                        createRouterLink("foo"),
                        new Element("p"),
                        createRouterLink("foo/bar"),
                        new Element("p"),
                        createRouterLink("./foobar"),
                        new Element("p"),
                        createRouterLink("./foobar?what=not"),
                        new Element("p"),
                        createRouterLink("./foobar?what=not#fragment"),
                        new Element("p"),
                        createRouterLink("/view/baz"),
                        new Element("p"),
                        // outside
                        ElementFactory.createDiv("outside this servlet"),
                        createRouterLink("/run"),
                        new Element("p"),
                        createRouterLink("/foo/bar"),
                        new Element("p"),
                        // external
                        ElementFactory.createDiv("external"),
                        createRouterLink("http://example.net/"));
    }

    private Element createRouterLink(String target) {
        return ElementFactory.createRouterLink(target, target);
    }
}
