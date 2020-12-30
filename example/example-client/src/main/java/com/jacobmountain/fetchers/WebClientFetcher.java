package com.jacobmountain.fetchers;

import com.jacobmountain.dto.Error;
import com.jacobmountain.dto.Mutation;
import com.jacobmountain.dto.Query;
import com.jacobmountain.graphql.client.ReactiveFetcher;
import com.jacobmountain.graphql.client.dto.Request;
import com.jacobmountain.graphql.client.dto.Response;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class WebClientFetcher implements ReactiveFetcher<Query, Mutation, Error> {

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

    private <T, A> Publisher<Response<T, Error>> doRequest(String query, A args, ParameterizedTypeReference<Response<T, Error>> typeReference) {
        return web.post()
                .bodyValue(new Request<>(query, args))
                .retrieve()
                .bodyToMono(typeReference);
    }

}
