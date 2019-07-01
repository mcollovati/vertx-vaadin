package com.github.mcollovati.vertx.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

public class HttpReverseProxy {

    private static final int DEFAULT_TIMEOUT = 120 * 1000;

    private final HttpClient client;

    public HttpReverseProxy(HttpClient client) {
        this.client = client;
    }


    public static HttpReverseProxy create(Vertx vertx, int devServerPort) {
        HttpClientOptions options = new HttpClientOptions()
            .setLogActivity(true)
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setIdleTimeout(DEFAULT_TIMEOUT)
            .setDefaultHost("localhost")
            .setDefaultPort(devServerPort);
        return new HttpReverseProxy(vertx.createHttpClient(options));
    }



    public static HttpRequestMessage encodeRequest(HttpServerRequest request) {
        return new HttpRequestMessage(request);
    }

    public static HttpResponseMessage encodeResponse(HttpClientResponse response) {
        return new HttpResponseMessage(response);
    }

    private static Map<String, String> toHeadersMap(JsonObject jsonObject) {
        Map<String, String> headers = new HashMap<>();
        jsonObject.getJsonObject("headers")
            .forEach(entry -> headers.put(entry.getKey(), Objects.toString(entry.getValue(), null)));
        return headers;
    }

    public static class HttpRequestMessage {
        private final JsonObject jsonObject;

        private HttpRequestMessage(HttpServerRequest request) {
            jsonObject = new JsonObject()
                .put("uri", request.uri())
                .put("method", request.method())
                .put("headers", new JsonObject());
            request.headers()
                .forEach(entry -> {
                    String value = "Connection".equals(entry.getKey()) ? "close" : entry.getValue();
                    jsonObject.getJsonObject("headers").put(entry.getKey(), value);
                });
        }

        private HttpRequestMessage(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public String uri() {
            return jsonObject.getString("uri");
        }

        public HttpMethod method() {
            return HttpMethod.valueOf(jsonObject.getString("method").toUpperCase());
        }
        public Map<String, String> headers() {
            return toHeadersMap(jsonObject);
        }


    }

    public static class HttpResponseMessage {
        private final JsonObject jsonObject;

        private HttpResponseMessage(HttpClientResponse response) {
            jsonObject = new JsonObject()
                .put("statusCode", response.statusCode())
                .put("headers", new JsonObject());
            response.headers().forEach(entry -> {
                jsonObject.getJsonObject("headers").put(entry.getKey(), entry.getValue());
            });
            response.bodyHandler(buffer -> jsonObject.put("body", buffer.getBytes()));
        }

        private HttpResponseMessage(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public int statusCode() {
            return jsonObject.getInteger("statusCode");
        }

        public Map<String, String> headers() {
            return toHeadersMap(jsonObject);
        }

        public Buffer body() {
            return Buffer.buffer(jsonObject.getBinary("body", new byte[0]));
        }

    }

    private static abstract class AbstractCoded<T> implements MessageCodec<T, T> {
        private final JsonObjectMessageCodec codec = new JsonObjectMessageCodec();
        private final Function<T, JsonObject> toJson;
        private final Function<JsonObject, T> fromJson;
        private final String name;

        public AbstractCoded(Function<T, JsonObject> toJson, Function<JsonObject, T> fromJson, String name) {
            this.toJson = toJson;
            this.fromJson = fromJson;
            this.name = name;
        }

        @Override
        public void encodeToWire(Buffer buffer, T message) {
            codec.encodeToWire(buffer, toJson.apply(message));
        }

        @Override
        public T decodeFromWire(int pos, Buffer buffer) {
            return fromJson.apply(codec.decodeFromWire(pos, buffer));
        }

        @Override
        public T transform(T message) {
            return message;
        }

        @Override
        public byte systemCodecID() {
            return -1;
        }

        @Override
        public String name() {
            return name;
        }

    }

    public static class HttpRequestMessageCoded extends AbstractCoded<HttpRequestMessage> {

        public static final String NAME = "httprequestmessage";

        public HttpRequestMessageCoded() {
            super(m -> m.jsonObject, HttpRequestMessage::new, NAME);
        }

    }

    public static class HttpResponseMessageCoded extends AbstractCoded<HttpResponseMessage> {

        public static final String NAME = "httpresponsemessage";

        public HttpResponseMessageCoded() {
            super(m -> m.jsonObject, HttpResponseMessage::new, NAME);
        }

    }

}
