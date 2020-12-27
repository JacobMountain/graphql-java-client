package co.uk.jacobmountain.modules;

import lombok.Builder;

@Builder
public class ClientDetails {

    private final boolean requiresSubscriber;

    private final boolean requiresFetcher;

    public boolean requiresSubscriber() {
        return requiresFetcher;
    }

    public boolean requiresFetcher() {
        return requiresFetcher;
    }

}
