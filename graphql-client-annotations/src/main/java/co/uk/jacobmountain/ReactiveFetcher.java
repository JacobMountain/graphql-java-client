package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Response;
import org.reactivestreams.Publisher;

public interface ReactiveFetcher<Q, M, S, Error> {

    <A> Publisher<Response<Q, Error>> query(String query, A args);

    <A> Publisher<Response<M, Error>> mutate(String mutation, A args);

    <A> Publisher<Response<S, Error>> subscribe(String subscription, A args);

}
