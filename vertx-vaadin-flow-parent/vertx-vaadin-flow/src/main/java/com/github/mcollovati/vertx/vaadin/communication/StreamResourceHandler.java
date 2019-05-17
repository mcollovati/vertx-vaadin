package com.github.mcollovati.vertx.vaadin.communication;

import javax.servlet.ServletContext;

import com.github.mcollovati.vertx.vaadin.StubServletContext;
import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Handles {@link StreamResource} instances registered in {@link VaadinSession}.
 *
 * Code adapted from the original {@link com.vaadin.flow.server.communication.StreamResourceHandler}
 */
public class StreamResourceHandler implements Serializable {

    /**
     * Handle sending for a stream resource request.
     *
     * @param session        session for the request
     * @param request        request to handle
     * @param response       response object to which a response can be written.
     * @param streamResource stream resource that handles data writer
     * @throws IOException if an IO error occurred
     */
    public void handleRequest(VaadinSession session, VaadinRequest request,
                              VaadinResponse response, StreamResource streamResource)
        throws IOException {

        StreamResourceWriter writer;
        session.lock();
        try {

            ServletContext context = new StubServletContext(((VertxVaadinRequest)request).getService());
            response.setContentType(streamResource.getContentTypeResolver()
                .apply(streamResource, context));
            response.setCacheTime(streamResource.getCacheTime());
            writer = streamResource.getWriter();
            if (writer == null) {
                throw new IOException(
                    "Stream resource produces null input stream");
            }
        } finally {
            session.unlock();
        }
        try (OutputStream outputStream = response.getOutputStream()) {
            writer.accept(outputStream, session);
        }
    }

}
