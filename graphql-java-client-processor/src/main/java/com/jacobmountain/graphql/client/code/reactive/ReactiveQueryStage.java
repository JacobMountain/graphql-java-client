package com.jacobmountain.graphql.client.code.reactive;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.code.AbstractQueryStage;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import org.reactivestreams.Publisher;

import java.util.Optional;

public class ReactiveQueryStage extends AbstractQueryStage {

    public ReactiveQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper) {
        super(queryGenerator, schema, typeMapper);
    }

    @Override
    public Optional<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        String member = method.isSubscription() ? "subscriber" : "fetcher";
        return Optional.of(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Publisher.class), getReturnTypeName(method)))
                        .add("$L.$L", member, getMethod(method)).add(generateQueryCode(method.getRequestName(), method))
                        .build()
        );
    }
}
