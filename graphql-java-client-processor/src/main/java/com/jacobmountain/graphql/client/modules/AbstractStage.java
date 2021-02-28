package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import graphql.language.ObjectTypeDefinition;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractStage {

    protected final Schema schema;

    protected final TypeMapper typeMapper;

    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        return Collections.emptyList();
    }

    public List<String> getTypeArguments() {
        return Collections.emptyList();
    }

    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        return Collections.emptyList();
    }

    protected ObjectTypeDefinition getTypeDefinition(MethodDetails details) {
        if (details.isQuery()) {
            return schema.getQuery();
        } else if (details.isMutation()) {
            return schema.getMutation();
        } else {
            return schema.getSubscription();
        }
    }

    @Value
    @Builder
    public static class MemberVariable {

        String name;

        TypeName type;

    }

}