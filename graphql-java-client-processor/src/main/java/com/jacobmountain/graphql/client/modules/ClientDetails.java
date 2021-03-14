package com.jacobmountain.graphql.client.modules;

import lombok.Builder;

@Builder
public class ClientDetails {

    private final boolean requiresSubscriber;

    private final boolean requiresFetcher;

    public boolean requiresSubscriber() {
        return requiresSubscriber;
    }

    public boolean requiresFetcher() {
        return requiresFetcher;
    }

}
