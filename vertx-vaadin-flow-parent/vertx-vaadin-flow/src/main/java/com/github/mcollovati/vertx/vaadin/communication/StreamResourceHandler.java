/*
 * The MIT License
 * Copyright © 2016-2019 Marco Collovati (mcollovati@gmail.com)
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
package com.github.mcollovati.vertx.vaadin.communication;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import com.github.mcollovati.vertx.vaadin.VertxVaadinRequest;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;

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

            ServletContext context = ((VertxVaadinRequest)request).getService().getServletContext();
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
