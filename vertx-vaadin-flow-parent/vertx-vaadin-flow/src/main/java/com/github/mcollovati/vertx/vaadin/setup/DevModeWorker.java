package com.github.mcollovati.vertx.vaadin.setup;

import com.github.mcollovati.vertx.http.HttpReverseProxy;
import com.vaadin.flow.server.DevModeHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevModeWorker extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(DevModeWorker.class);

    private static final String ADDRESS = "dev-mode-server";
    private static final int DEFAULT_TIMEOUT = 120 * 1000;

    private HttpClient httpClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().registerCodec(new HttpReverseProxy.HttpRequestMessageCoded());
        vertx.eventBus().registerCodec(new HttpReverseProxy.HttpResponseMessageCoded());

        int devServerPort = config().getInteger("port", 0);
        if (devServerPort > 0) {
            HttpClientOptions options = new HttpClientOptions()
                .setLogActivity(true)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setIdleTimeout(DEFAULT_TIMEOUT)
                .setDefaultHost("localhost")
                .setDefaultPort(devServerPort);
            httpClient = vertx.createHttpClient(options);
        }
        vertx.eventBus().localConsumer(ADDRESS, this::proxyRequest);
        startFuture.complete();
    }


    public static Handler<AsyncResult<Message<HttpReverseProxy.HttpResponseMessage>>> proxyResponseHandler(RoutingContext ctx) {
        return event -> {
            ctx.request().resume();
            if (event.succeeded()) {
                HttpReverseProxy.HttpResponseMessage clientResponse = event.result().body();
                if (clientResponse == null || clientResponse.statusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                    ctx.next();
                } else if (clientResponse.statusCode() == HttpResponseStatus.OK.code()) {
                    copyResponse(event.result().body(), ctx.response());
                } else {
                    ctx.fail(clientResponse.statusCode());
                }
            } else {
                ctx.fail(event.cause());
            }
        };
    }

    public static void copyResponse(HttpReverseProxy.HttpResponseMessage clientResponse, HttpServerResponse serverResponse) {
        clientResponse.headers().forEach(serverResponse::putHeader);
        clientResponse.headers().forEach( (k,v) -> System.out.println("====================== HEADER " + k + " = " + v) );
        serverResponse.setStatusCode(clientResponse.statusCode());
        Buffer body = clientResponse.body();
        //System.out.println("=====================================" + body.toString("UTF-8"));
        serverResponse.bodyEndHandler(event -> System.out.println("======================== response sent"));
        serverResponse.end(body);
    }


    private void proxyRequest(Message<HttpReverseProxy.HttpRequestMessage> message) {
        if (httpClient != null) {
            forwardRequest(message.body(), resp -> message.reply(resp, new DeliveryOptions().setCodecName(HttpReverseProxy.HttpResponseMessageCoded.NAME)));
        } else {
            message.reply(null);
        }
    }

    public static MessageProducer<HttpReverseProxy.HttpRequestMessage> forwarder(Vertx vertx) {
        DeliveryOptions deliveryOptions = new DeliveryOptions()
            .setCodecName(HttpReverseProxy.HttpRequestMessageCoded.NAME)
            .setSendTimeout(DEFAULT_TIMEOUT);
        return vertx.eventBus().publisher(ADDRESS, deliveryOptions);
    }

    private void forwardRequest(HttpReverseProxy.HttpRequestMessage request, Handler<HttpReverseProxy.HttpResponseMessage> handler) {
        HttpClientRequest forwardRequest = httpClient.request(request.method(), request.uri().replaceFirst(".*/VAADIN/", "/"));
        forwardRequest.handler(event -> {
            event.pause();
            handler.handle(HttpReverseProxy.encodeResponse(event));
            event.resume();
        });
        request.headers()
            .forEach((key, value) -> {
                String valueOk = "Connection".equals(key) ? "close" : value;
                forwardRequest.putHeader(key, valueOk);
            });
        logger.debug("Requesting resource to webpack {}", request.uri());
        forwardRequest.end();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
        stopFuture.complete();
    }

}
