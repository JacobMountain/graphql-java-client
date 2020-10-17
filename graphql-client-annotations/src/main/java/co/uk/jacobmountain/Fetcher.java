package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Response;

public interface Fetcher<Q, M> {

    <A> Response<Q> query(String query, A args);

    <A> Response<M> mutate(String mutation, A args);

    default Response<Q> query(String query) {
        return query(query, null);
    }

}
