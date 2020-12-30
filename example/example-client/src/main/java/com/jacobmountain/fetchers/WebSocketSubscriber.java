package com.jacobmountain.fetchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacobmountain.ReactiveSubscriber;
import com.jacobmountain.dto.Error;
import com.jacobmountain.dto.Request;
import com.jacobmountain.dto.Response;
import com.jacobmountain.dto.Subscription;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.net.URISyntaxException;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;


public class WebSocketSubscriber implements ReactiveSubscriber<Subscription, Error> {

    private final URI subscriptions;

    public WebSocketSubscriber(String url) throws URISyntaxException {
        this.subscriptions = new URI(url);
    }

    @Override
    @SneakyThrows
    public <A> Publisher<Response<Subscription, Error>> subscribe(String query, A args) {
        Sinks.Many<Response<Subscription, Error>> many = Sinks.many()
                .multicast()
                .onBackpressureBuffer();
        new SubscriptionClient<>(this.subscriptions, new Request<>(query, args), many);
        return many.asFlux();
    }

    @Slf4j
    static class SubscriptionClient<A> extends WebSocketClient {

        private final Sinks.Many<Response<Subscription, Error>> sink;

        private final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        @SneakyThrows
        public SubscriptionClient(URI serverUri, Request<A> request, Sinks.Many<Response<Subscription, Error>> sink) {
            super(serverUri);
            this.sink = sink;
            this.connectBlocking();
            send(request);
        }

        @SneakyThrows
        private <T> void send(T payload) {
            this.send(objectMapper.writeValueAsString(payload));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("");
        }

        @Override
        public void onMessage(String message) {
            Response<Subscription, Error> response = null;
            try {
                response = objectMapper.readValue(message, new TypeReference<Response<Subscription, Error>>() {
                });
            } catch (JsonProcessingException e) {
                log.error("Error deserializing websocket message", e);
            }
            sink.emitNext(response, FAIL_FAST);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("");
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("");
        }
    }
}
