package com.jacobmountain.graphql.client.query;

import graphql.language.FieldDefinition;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
@AllArgsConstructor
public class QueryContext {

    int depth;

    FieldDefinition fieldDefinition;

    Set<String> params;

    Set<String> visited;

    QueryContext increment() {
        return new QueryContext(depth + 1, fieldDefinition, params, new HashSet<>());
    }

    QueryContext withType(FieldDefinition fieldDefinition) {
        return new QueryContext(depth, fieldDefinition, params, visited);
    }

    QueryContext withVisited(Set<String> visited) {
        return new QueryContext(depth, fieldDefinition, params, visited);
    }

}
