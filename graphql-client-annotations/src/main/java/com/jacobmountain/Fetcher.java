package com.jacobmountain;

import com.jacobmountain.dto.Response;

public interface Fetcher<Q, M, Error> {

    <A> Response<Q, Error> query(String query, A args);

    <A> Response<M, Error> mutate(String mutation, A args);

}
