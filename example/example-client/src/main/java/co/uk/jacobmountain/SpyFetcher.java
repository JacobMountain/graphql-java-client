package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Response;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpyFetcher<Query, Mutation> implements Fetcher<Query, Mutation> {

    private final Fetcher<Query, Mutation> delegate;

    private int queries = 0;
    private int mutations = 0;

    @Override
    public <A> Response<Query> query(String query, A args) {
        queries++;
        return delegate.query(query, args);
    }

    @Override
    public <A> Response<Mutation> mutate(String mutation, A args) {
        mutations++;
        return delegate.mutate(mutation, args);
    }

    boolean hasInteractions() {
        return queries > 0 || mutations > 0;
    }

}
