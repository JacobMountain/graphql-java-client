package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.ReactiveFetcher;
import com.jacobmountain.graphql.client.ReactiveSubscriber;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactiveQueryStage extends AbstractQueryStage {

    public ReactiveQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper, String dtoPackageName) {
        super(queryGenerator, schema, typeMapper, dtoPackageName);
    }

    @Override
    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        ArrayList<MemberVariable> vars = new ArrayList<>();
        if (details.requiresFetcher()) {
            vars.add(
                    MemberVariable.builder()
                            .name("fetcher")
                            .type(getFetcherTypeName(ReactiveFetcher.class))
                            .build()
            );
        }
        if (details.requiresSubscriber()) {
            vars.add(
                    MemberVariable.builder()
                            .name("subscriber")
                            .type(getSubscriberTypeName(ReactiveSubscriber.class))
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
                        .add("$L.$L", member, getMethod(method)).add(generateQueryCode(method.getRequestName(), method))
                        .build()
        );
    }
}
