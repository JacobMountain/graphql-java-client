package com.jacobmountain.graphql.client.visitor;

import com.squareup.javapoet.ClassName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ClientDetails {

    private String name;

    private ClassName clientInterface;

    private boolean requiresSubscriber;

    private boolean requiresFetcher;

    private List<MethodDetails> methods;

    public boolean requiresSubscriber() {
        return requiresSubscriber;
    }

    public boolean requiresFetcher() {
        return requiresFetcher;
    }

}
