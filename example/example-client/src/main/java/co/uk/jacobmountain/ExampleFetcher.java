package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Query;
import co.uk.jacobmountain.dto.Request;
import co.uk.jacobmountain.dto.Response;
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
public class ExampleFetcher implements Fetcher<Query, Void, Error> {

    private final RestTemplate template;

    public ExampleFetcher(String url) {
        this.template = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.template.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
        this.template.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));
    }

    @Override
    public <A> Response<Query, Error> query(String s, A a) {
        Response<Query, Error> body = template.exchange(
                "/graph",
                HttpMethod.POST,
                new HttpEntity<>(new Request<>(s, a)),
                new ParameterizedTypeReference<Response<Query, Error>>() {
                }
        ).getBody();
        printErrors(body);
        return body;
    }

    @Override
    public <A> Response<Void, Error> mutate(String s, A a) {
        return template.exchange(
                "/graph",
                HttpMethod.POST,
                new HttpEntity<>(new Request<>(s, a)),
                new ParameterizedTypeReference<Response<Void, Error>>() {
                }
        ).getBody();
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
