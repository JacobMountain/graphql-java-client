package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Query;
import co.uk.jacobmountain.dto.Request;
import co.uk.jacobmountain.dto.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

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
                "/graph",
                HttpMethod.POST,
                new HttpEntity<>(new Request<>(s, a)),
                new ParameterizedTypeReference<Response<Query>>() {
                }
        ).getBody();
    }

    @Override
    public <A> Response<Void> mutate(String s, A a) {
        return template.exchange(
                "/graph",
                HttpMethod.POST,
                new HttpEntity<>(new Request<>(s, a)),
                new ParameterizedTypeReference<Response<Void>>() {
                }
        ).getBody();
    }

}
