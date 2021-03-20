package com.jacobmountain.graphql.client.code;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Assembler {

    private static final TypeVariableName ERROR_TYPE_VARIABLE = TypeVariableName.get("Error");

    private final TypeName fetcherInterface;

    private final TypeName subscriberInterface;

    protected final AbstractStage arguments;

    protected final AbstractStage query;

    protected final AbstractStage returnResults;

    protected Assembler(AbstractStage query,
                        AbstractStage returnResults,
                        Schema schema,
                        TypeMapper typeMapper,
                        Class<?> fetcherInterface,
                        Class<?> subscriberInterface) {
        this.arguments = new ArgumentAssemblyStage();
        this.query = query;
        this.returnResults = returnResults;
        final TypeName queryType = typeMapper.getType(schema.getQueryTypeName());
        final TypeName mutationType = schema.getMutationTypeName()
                .map(typeMapper::getType)
                .orElse(ClassName.get(Void.class));
        final TypeName subscriptionType = schema.getSubscriptionTypeName()
                .map(typeMapper::getType)
                .orElse(ClassName.get(Void.class));
        this.fetcherInterface = ParameterizedTypeName.get(
                ClassName.get(fetcherInterface),
                queryType,
                mutationType,
                ERROR_TYPE_VARIABLE
        );
        this.subscriberInterface = ParameterizedTypeName.get(
                ClassName.get(subscriberInterface),
                subscriptionType,
                ERROR_TYPE_VARIABLE
        );
    }

    public Collection<TypeVariableName> getTypeArguments() {
        return Collections.singleton(ERROR_TYPE_VARIABLE);
    }

    public List<MemberVariable> getMemberVariables(ClientDetails client) {
        ArrayList<MemberVariable> vars = new ArrayList<>(2);
        if (client.requiresFetcher()) {
            vars.add(
                    MemberVariable.builder()
                            .name("fetcher")
                            .type(fetcherInterface)
                            .build()
            );
        }
        if (client.requiresSubscriber()) {
            vars.add(
                    MemberVariable.builder()
                            .name("subscriber")
                            .type(subscriberInterface)
                            .build()
            );
        }
        return vars;
    }

    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        List<CodeBlock> code = new ArrayList<>(3);
        this.arguments.assemble(client, method).ifPresent(code::add);
        this.query.assemble(client, method).ifPresent(code::add);
        this.returnResults.assemble(client, method).ifPresent(code::add);
        return code;
    }

}
