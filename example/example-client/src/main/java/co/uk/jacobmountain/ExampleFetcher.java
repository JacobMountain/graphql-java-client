package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Query;
import co.uk.jacobmountain.dto.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.util.Collections;

public class ExampleFetcher implements Fetcher<Query, Void> {

    private final RestTemplate template;

    public ExampleFetcher(String url) {
        this.template = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.template.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
        this.template.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));
    }

    @Override
    public <A> Response<Query> query(String s, A a) {
        return template.exchange(
                new RequestEntity<>(new Request<>(s, a), HttpMethod.POST, URI.create("http://localhost:8080/graph")),
                new ParameterizedTypeReference<Response<Query>>() {
                }
        ).getBody();
    }

    @Override
    public <A> Response<Void> mutate(String s, A a) {
        return template.exchange(
                new RequestEntity<>(new Request<>(s, a), HttpMethod.POST, URI.create("http://localhost:8080/graph")),
                new ParameterizedTypeReference<Response<Void>>() {
                }
        ).getBody();
    }

    static class Request<A> {

        private final String query;

        private final A variables;

        public Request(String query, A variables) {
            this.query = query;
            this.variables = variables;
        }

        public String getQuery() {
            return query;
        }

        public A getVariables() {
            return variables;
        }
    }
}
