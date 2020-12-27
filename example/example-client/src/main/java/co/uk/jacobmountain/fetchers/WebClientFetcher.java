package co.uk.jacobmountain.fetchers;

import co.uk.jacobmountain.ReactiveFetcher;
import co.uk.jacobmountain.dto.Error;
import co.uk.jacobmountain.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

@RequiredArgsConstructor
public class WebClientFetcher implements ReactiveFetcher<Query, Mutation, Subscription, Error> {

    private final WebClient web;

    private final URI subscriptions;

    private final Set<SubscriptionClient<?>> clients = new HashSet<>();

    public WebClientFetcher(String url, String subscriptionUrl) throws URISyntaxException {
        this.subscriptions = new URI(subscriptionUrl + "/subscriptions");
        web = WebClient.builder()
                .baseUrl(url)
                .build();
    }

    @Override
    public <A> Publisher<Response<Query, Error>> query(String query, A args) {
        return doRequest(query, args, new ParameterizedTypeReference<Response<Query, Error>>() {
        });
    }

    @Override
    public <A> Publisher<Response<Mutation, Error>> mutate(String mutation, A args) {
        return doRequest(mutation, args, new ParameterizedTypeReference<Response<Mutation, Error>>() {
        });
    }

    @Override
    @SneakyThrows
    public <A> Publisher<Response<Subscription, Error>> subscribe(String query, A args) {
        Sinks.Many<Response<Subscription, Error>> many = Sinks.many()
                .multicast()
                .onBackpressureBuffer();
        clients.add(new SubscriptionClient<>(this.subscriptions, new Request<>(query, args), many));
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
            String text = objectMapper.writeValueAsString(payload);
            log.info("ws: {}", text);
            this.send(text);
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

    private <T, A> Publisher<Response<T, Error>> doRequest(String query, A args, ParameterizedTypeReference<Response<T, Error>> typeReference) {
        return web.post()
                .uri("/graph")
                .bodyValue(new Request<>(query, args))
                .retrieve()
                .bodyToMono(typeReference);
    }

}
