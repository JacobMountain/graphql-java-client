package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public class ClientDetails {

    private final boolean requiresSubscriber;

    private final boolean requiresFetcher;

    @Getter
    private final List<GraphQLFragment> fragments;

    public boolean requiresSubscriber() {
        return requiresFetcher;
    }

    public boolean requiresFetcher() {
        return requiresFetcher;
    }

}
