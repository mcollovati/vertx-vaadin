package com.github.mcollovati.vertx.vaadin.communication;

import java.io.IOException;

import com.github.mcollovati.vertx.vaadin.VertxVaadinResponse;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.UidlRequestHandler;

/**
 * A Replacement for {@link UidlRequestHandler} that closes response before
 * session unlock to ensure request messages are sent to the client
 * before push is invoked.
 */
public class VertxUidlRequestHandler extends UidlRequestHandler {

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        boolean result = super.synchronizedHandleRequest(session, request, response);
        if (result && response instanceof VertxVaadinResponse) {
            // end request in order to send messages to the client immediately
            // this is manly needed to ensure correct messages order when using push
            ((VertxVaadinResponse) response).end();
        }
        return result;
    }
}
