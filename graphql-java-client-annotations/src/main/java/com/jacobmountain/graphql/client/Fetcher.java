package com.jacobmountain.graphql.client;

import com.jacobmountain.graphql.client.dto.Response;

public interface Fetcher<Q, M, Error> {

    <A> Response<Q, Error> query(String query, A args);

    <A> Response<M, Error> mutate(String mutation, A args);

}
