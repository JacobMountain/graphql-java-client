package co.uk.jacobmountain.fetchers;

import co.uk.jacobmountain.ReactiveFetcher;
import co.uk.jacobmountain.dto.Error;
import co.uk.jacobmountain.dto.*;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class WebClientFetcher implements ReactiveFetcher<Query, Mutation, Subscription, Error> {

    private final WebClient web;

    public WebClientFetcher(String url) {
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
    public <A> Publisher<Response<Subscription, Error>> subscribe(String query, A args) {
        return doRequest(query, args, new ParameterizedTypeReference<Response<Subscription, Error>>() {
        });
    }

    private <T, A> Publisher<Response<T, Error>> doRequest(String query, A args, ParameterizedTypeReference<Response<T, Error>> typeReference) {
        return web.post()
                .uri("/graph")
                .bodyValue(new Request<>(query, args))
                .retrieve()
                .bodyToMono(typeReference);
    }

}
