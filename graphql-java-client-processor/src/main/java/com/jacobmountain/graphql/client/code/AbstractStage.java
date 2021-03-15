package com.jacobmountain.graphql.client.code;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.CodeBlock;
import graphql.language.ObjectTypeDefinition;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractStage {

    protected final Schema schema;

    protected final TypeMapper typeMapper;

    public abstract Optional<CodeBlock> assemble(ClientDetails client, MethodDetails method);

    protected ObjectTypeDefinition getTypeDefinition(MethodDetails details) {
        if (details.isQuery()) {
            return schema.getQuery();
        } else if (details.isMutation()) {
            return schema.getMutation();
        } else {
            return schema.getSubscription();
        }
    }

}