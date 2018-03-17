package com.github.mcollovati.vertx.vaadin.sockjs.communication;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.communication.PushConnection;
import com.vaadin.flow.server.communication.PushConnectionFactory;

public class SockJSPushConnectionFactory implements PushConnectionFactory {
    @Override
    public PushConnection apply(UI ui) {
        return new SockJSPushConnection(ui);
    }
}
