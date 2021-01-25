package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.ReactiveFetcher;
import com.jacobmountain.graphql.client.ReactiveSubscriber;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.*;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactiveQueryStage extends AbstractQueryStage {

    public ReactiveQueryStage(Schema schema, TypeMapper typeMapper, String dtoPackageName) {
        super(schema, typeMapper, dtoPackageName);
    }

    private TypeName getFetcherTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(ReactiveFetcher.class),
                query,
                mutation,
                TypeVariableName.get("Error")
        );
    }

    private TypeName getSubscriberTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(ReactiveSubscriber.class),
                subscription,
                TypeVariableName.get("Error")
        );
    }

    @Override
    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        ArrayList<MemberVariable> vars = new ArrayList<>();
        if (details.requiresFetcher()) {
            vars.add(
                    MemberVariable.builder()
                            .name("fetcher")
                            .type(getFetcherTypeName())
                            .build()
            );
        }
        if (details.requiresSubscriber()) {
            vars.add(
                    MemberVariable.builder()
                            .name("subscriber")
                            .type(getSubscriberTypeName())
                            .build()
            );
        }
        return vars;
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        String member = method.isSubscription() ? "subscriber" : "fetcher";
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Publisher.class), getReturnTypeName(method)))
                        .add("$L.$L", member, getMethod(method)).add(generateQueryCode(method.getRequestName(), client, method))
                        .build()
        );
    }
}
