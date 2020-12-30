package com.jacobmountain.fetchers;

import com.jacobmountain.Fetcher;
import com.jacobmountain.dto.Response;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpyFetcher<Query, Mutation, Error> implements Fetcher<Query, Mutation, Error> {

    private final Fetcher<Query, Mutation, Error> delegate;

    private int queries = 0;
    private int mutations = 0;

    @Override
    public <A> Response<Query, Error> query(String query, A args) {
        queries++;
        return delegate.query(query, args);
    }

    @Override
    public <A> Response<Mutation, Error> mutate(String mutation, A args) {
        mutations++;
        return delegate.mutate(mutation, args);
    }

    boolean hasInteractions() {
        return queries > 0 || mutations > 0;
    }

}
