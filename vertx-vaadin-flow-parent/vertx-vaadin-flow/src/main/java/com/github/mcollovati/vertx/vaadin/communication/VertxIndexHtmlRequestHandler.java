package com.github.mcollovati.vertx.vaadin.communication;

import javax.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.github.mcollovati.vertx.vaadin.VertxBootstrapHandler;
import com.vaadin.flow.internal.BootstrapHandlerHelper;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.IndexHtmlRequestHandler;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class VertxIndexHtmlRequestHandler extends IndexHtmlRequestHandler {

    @Override
    protected boolean canHandleRequest(VaadinRequest request) {
        return !VertxBootstrapHandler.isFrameworkInternalRequest(request) && request
            .getService().getBootstrapUrlPredicate().isValidUrl(request);
    }

    @Override
    public boolean synchronizedHandleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response) throws IOException {
        VaadinResponseWrapper wrapper = new VaadinResponseWrapper(response);
        boolean result = super.synchronizedHandleRequest(session, request, wrapper);
        if (result) {
            try {
                // Websocket URL used for gizmo should go directly to sockjs raw websocket handler
                String pushUrl = BootstrapHandlerHelper.getPushURL(session, request)
                    .replaceFirst("/$", "")
                    + "/websocket";
                // dirty hack
                pushUrl = pushUrl.replaceFirst("/nullVAADIN/", "/VAADIN/");
                String output = wrapper.replaceGizmoUrl(pushUrl);
                response.getOutputStream()
                    .write(output.getBytes(UTF_8));
            } catch (IOException e) {
                LoggerFactory.getLogger(VertxIndexHtmlRequestHandler.class).error("Error writing 'index.html' to response", e);
                return false;
            }
        }
        return result;
    }

    private static class VaadinResponseWrapper implements VaadinResponse {

        private final VaadinResponse delegate;
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        public VaadinResponseWrapper(VaadinResponse delegate) {
            this.delegate = delegate;
        }

        String replaceGizmoUrl(String url) {
            String html = outputStream.toString(UTF_8);
            return html
                .replaceFirst("(<vaadin-dev-tools.*url=\")([^\"]+)(\".*></vaadin-dev-tools>)",
                    "$1" + url + "$3");
        }

        @Override
        public void setStatus(int statusCode) {
            delegate.setStatus(statusCode);
        }

        @Override
        public void setContentType(String contentType) {
            delegate.setContentType(contentType);
        }

        @Override
        public void setHeader(String name, String value) {
            delegate.setHeader(name, value);
        }

        @Override
        public void setDateHeader(String name, long timestamp) {
            delegate.setDateHeader(name, timestamp);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(outputStream);
        }

        @Override
        public void setCacheTime(long milliseconds) {
            delegate.setCacheTime(milliseconds);
        }

        @Override
        public void sendError(int errorCode, String message) throws IOException {
            delegate.sendError(errorCode, message);
        }

        @Override
        public VaadinService getService() {
            return delegate.getService();
        }

        @Override
        public void addCookie(Cookie cookie) {
            delegate.addCookie(cookie);
        }

        @Override
        public void setContentLength(int len) {
            delegate.setContentLength(len);
        }

        @Override
        public void setNoCacheHeaders() {
            delegate.setNoCacheHeaders();
        }

        public static VaadinResponse getCurrent() {
            return VaadinResponse.getCurrent();
        }

    }
}
