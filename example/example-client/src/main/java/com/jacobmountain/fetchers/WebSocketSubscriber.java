package com.jacobmountain.fetchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacobmountain.dto.Error;
import com.jacobmountain.dto.Subscription;
import com.jacobmountain.graphql.client.Subscriber;
import com.jacobmountain.graphql.client.dto.Request;
import com.jacobmountain.graphql.client.dto.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;


public class WebSocketSubscriber implements Subscriber<Subscription, Error> {

    private final URI subscriptions;

    public WebSocketSubscriber(String url) throws URISyntaxException {
        this.subscriptions = new URI(url);
    }

    @Override
    public <A> void subscribe(String subscription, A args, Consumer<Response<Subscription, Error>> callback) {
        new SubscriptionClient<>(this.subscriptions, new Request<>(subscription, args), callback);
    }

    @Slf4j
    static class SubscriptionClient<A> extends WebSocketClient {

        private final Consumer<Response<Subscription, Error>> callback;

        private final ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        @SneakyThrows
        public SubscriptionClient(URI serverUri, Request<A> request, Consumer<Response<Subscription, Error>> callback) {
            super(serverUri);
            this.callback = callback;
            this.connectBlocking();
            send(request);
        }

        @SneakyThrows
        private <T> void send(T payload) {
            this.send(objectMapper.writeValueAsString(payload));
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {

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
            callback.accept(response);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }
}
