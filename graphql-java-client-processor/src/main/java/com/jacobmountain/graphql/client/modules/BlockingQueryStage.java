package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.Fetcher;
import com.jacobmountain.graphql.client.Subscriber;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import graphql.language.ObjectTypeDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlockingQueryStage extends AbstractQueryStage {

    private static final ClassName RESPONSE_CLASS_NAME = ClassName.get(Response.class);

    public BlockingQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper, String dtoPackageName) {
        super(queryGenerator, schema, typeMapper, dtoPackageName);
    }

    @Override
    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        ArrayList<MemberVariable> vars = new ArrayList<>();
        if (details.requiresFetcher()) {
            vars.add(
                    MemberVariable.builder()
                            .name("fetcher")
                            .type(getFetcherTypeName(Fetcher.class))
                            .build()
            );
        }
        if (details.requiresSubscriber()) {
            vars.add(
                    MemberVariable.builder()
                            .name("subscriber")
                            .type(getSubscriberTypeName(Subscriber.class))
                            .build()
            );
        }
        return vars;
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        String member = method.isSubscription() ? "subscriber" : "fetcher";
        ObjectTypeDefinition query = getTypeDefinition(method);
        if (method.isSubscription()) {
            final CodeBlock unwrapLambda = CodeBlock.builder()
                    .add("subscription -> ")
                    .add("$T.ofNullable(subscription)", Optional.class)
                    .add(".map($T::getData)", RESPONSE_CLASS_NAME)
                    .add(".map($T::$L)", typeMapper.getType(query.getName()), StringUtils.camelCase("get", method.getField()))
                    .add(".ifPresent(callback)")
                    .build();
            final CodeBlock queryCode = generateQueryCode(method.getRequestName(), method, unwrapLambda);
            return Collections.singletonList(
                    CodeBlock.builder()
                            .add("$L.$L", member, getMethod(method)).add(queryCode)
                            .build()
            );
        }
        final CodeBlock queryCode = generateQueryCode(method.getRequestName(), method);
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(query.getName()), TypeVariableName.get("Error")))
                        .add("$L.$L", member, getMethod(method)).add(queryCode)
                        .build()
        );
    }

}