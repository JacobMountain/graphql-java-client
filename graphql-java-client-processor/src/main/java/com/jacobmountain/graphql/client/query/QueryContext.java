package com.jacobmountain.graphql.client.query;

import graphql.language.FieldDefinition;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
@AllArgsConstructor
public class QueryContext {

    QueryContext parent;

    int depth;

    FieldDefinition fieldDefinition;

    Set<String> params;

    QueryContext increment() {
        return new QueryContext(this, depth + 1, fieldDefinition, params);
    }

    QueryContext withType(FieldDefinition fieldDefinition) {
        return new QueryContext(parent, depth, fieldDefinition, params);
    }

}
