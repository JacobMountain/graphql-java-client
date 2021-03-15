package com.jacobmountain.graphql.client.code.blocking;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.code.AbstractQueryStage;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.exceptions.MissingAnnotationException;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import graphql.language.ObjectTypeDefinition;

import java.util.Optional;

public class BlockingQueryStage extends AbstractQueryStage {

    private static final ClassName RESPONSE_CLASS_NAME = ClassName.get(Response.class);

    public BlockingQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper) {
        super(queryGenerator, schema, typeMapper);
    }

    @Override
    public Optional<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        String member = method.isSubscription() ? "subscriber" : "fetcher";
        ObjectTypeDefinition query = getTypeDefinition(method);
        final CodeBlock.Builder builder = CodeBlock.builder();
        CodeBlock queryCode;
        if (method.isSubscription()) {
            final CodeBlock unwrapLambda = unwrapResponseLambda(query, method);
            queryCode = generateQueryCode(method.getRequestName(), method, unwrapLambda);
        } else {
            queryCode = generateQueryCode(method.getRequestName(), method);
            builder.add("$T thing = ", ParameterizedTypeName.get(RESPONSE_CLASS_NAME, typeMapper.getType(query.getName()), TypeVariableName.get("Error")));
        }
        builder.add("$L.$L", member, getMethod(method)).add(queryCode);
        return Optional.of(builder.build());
    }

    private CodeBlock unwrapResponseLambda(ObjectTypeDefinition query, MethodDetails method) {
        return CodeBlock.builder()
                .add("subscription -> ")
                .add("$T.ofNullable(subscription)", Optional.class)
                .add(".map($T::getData)", RESPONSE_CLASS_NAME)
                .add(".map($T::$L)", typeMapper.getType(query.getName()), StringUtils.camelCase("get", method.getField()))
                .add(".ifPresent($L)", method.getSubscriptionCallback().orElseThrow(() -> new MissingAnnotationException("Expected a callback parameter to marked with @GraphQLSubscriptionCallback")))
                .build();
    }

}