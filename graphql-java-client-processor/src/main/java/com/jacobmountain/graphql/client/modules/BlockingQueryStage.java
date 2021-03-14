package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.Fetcher;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import graphql.language.ObjectTypeDefinition;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockingQueryStage extends AbstractQueryStage {

    public BlockingQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper, String dtoPackageName) {
        super(queryGenerator, schema, typeMapper, dtoPackageName);
    }

    private ParameterizedTypeName generateTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(Fetcher.class),
                query,
                mutation,
                TypeVariableName.get("Error")
        );
    }

    @Override
    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        return Stream.of(
                MemberVariable.builder()
                        .name("fetcher")
                        .type(generateTypeName())
                        .build()
        )
                .filter(it -> details.requiresFetcher())
                .collect(Collectors.toList());
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        ObjectTypeDefinition query = getTypeDefinition(method);
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(query.getName()), TypeVariableName.get("Error")))
                        .add("fetcher.$L", getMethod(method)).add(generateQueryCode(method.getRequestName(), method))
                        .build()
        );
    }

}