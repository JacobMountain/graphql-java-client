package com.jacobmountain.fetchers;

import com.jacobmountain.Fetcher;
import com.jacobmountain.dto.Error;
import com.jacobmountain.dto.*;
import com.jacobmountain.logging.LoggingRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Collections;

@Slf4j
public class RestTemplateFetcher implements Fetcher<Query, Mutation, Error> {

    private final RestTemplate template;

    public RestTemplateFetcher(String url) {
        this.template = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.template.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
        this.template.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));
    }

    @Override
    public <A> Response<Query, Error> query(String s, A a) {
        return makeRequest(
                new Request<>(s, a),
                new ParameterizedTypeReference<Response<Query, Error>>() {
                }
        );
    }

    @Override
    public <A> Response<Mutation, Error> mutate(String s, A a) {
        return makeRequest(
                new Request<>(s, a),
                new ParameterizedTypeReference<Response<Mutation, Error>>() {
                }
        );
    }

    private <T, V> Response<T, Error> makeRequest(Request<V> request, ParameterizedTypeReference<Response<T, Error>> typeReference) {
        try {
            Response<T, Error> response = template.exchange(
                    "/graph",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    typeReference
            ).getBody();
            printErrors(response);
            return response;
        } catch (Exception e) {
            return new Response<>(null, Collections.singletonList(new Error(e.getMessage())));
        }
    }

    private <T> void printErrors(Response<T, Error> response) {
        if (!CollectionUtils.isEmpty(response.getErrors())) {
            response.getErrors().forEach(this::printError);
        }
    }

    private void printError(Error error) {
        log.error(error.getMessage());
    }

}
