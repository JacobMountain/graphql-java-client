package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.jacobmountain.graphql.client.visitor.Parameter;
import com.squareup.javapoet.*;
import graphql.language.ObjectTypeDefinition;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractQueryStage extends AbstractStage {

    protected final TypeName query;

    protected final TypeName mutation;

    protected final TypeName subscription;

    private final QueryGenerator queryGenerator;

    public AbstractQueryStage(QueryGenerator queryGenerator, Schema schema, TypeMapper typeMapper, String dtoPackageName) {
        super(schema, typeMapper);
        this.query = ClassName.get(dtoPackageName, schema.getQueryTypeName());
        this.mutation = schema.getMutationTypeName().map(it -> ClassName.get(dtoPackageName, it)).orElse(ClassName.get(Void.class));
        this.subscription = schema.getSubscriptionTypeName().map(it -> ClassName.get(dtoPackageName, it)).orElse(ClassName.get(Void.class));
        this.queryGenerator = queryGenerator;
    }

    protected TypeName getFetcherTypeName(Class<?> fetcher) {
        return ParameterizedTypeName.get(
                ClassName.get(fetcher),
                query,
                mutation,
                TypeVariableName.get("Error")
        );
    }

    protected TypeName getSubscriberTypeName(Class<?> fetcher) {
        return ParameterizedTypeName.get(
                ClassName.get(fetcher),
                subscription,
                TypeVariableName.get("Error")
        );
    }


    @Override
    public List<String> getTypeArguments() {
        return Collections.singletonList("Error");
    }

    protected TypeName getReturnTypeName(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = getTypeDefinition(details);
        return ParameterizedTypeName.get(
                ClassName.get(Response.class),
                typeMapper.getType(typeDefinition.getName()),
                TypeVariableName.get("Error")
        );
    }

    protected String getMethod(MethodDetails details) {
        String method = "query";
        if (details.isMutation()) {
            method = "mutate";
        } else if (details.isSubscription()) {
            method = "subscribe";
        }
        return method;
    }

    protected CodeBlock generateQueryCode(String request, MethodDetails method, CodeBlock... additionalArgs) {
        Set<String> params = method.getParameters()
                .stream()
                .map(Parameter::getField)
                .collect(Collectors.toSet());
        QueryGenerator.QueryBuilder builder;
        if (method.isQuery()) {
            builder = queryGenerator.query();
        } else if (method.isMutation()) {
            builder = queryGenerator.mutation();
        } else if (method.isSubscription()) {
            builder = queryGenerator.subscription();
        } else {
            throw new IllegalStateException();
        }

        String query = builder
                .select(
                        method.getSelection()
                                .stream()
                                .map(GraphQLFieldSelection::new)
                                .collect(Collectors.toList())
                )
                .maxDepth(method.getMaxDepth())
                .build(request, method.getField(), params);

        List<CodeBlock> args = new ArrayList<>();
        args.add(CodeBlock.of("\n\"$L\"", query));
        args.add(CodeBlock.of("\n$L", method.hasParameters() ? "args" : "null"));
        args.addAll(Arrays.asList(additionalArgs));
        return CodeBlock.builder()
                .add("(")
                .add(CodeBlock.join(args, ", "))
                .add(")")
                .build();
    }

}