package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Response;

public interface Fetcher<Q, M, Error> {

    <A> Response<Q, Error> query(String query, A args);

    <A> Response<M, Error> mutate(String mutation, A args);

}
