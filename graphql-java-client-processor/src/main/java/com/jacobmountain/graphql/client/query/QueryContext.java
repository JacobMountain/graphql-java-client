package com.jacobmountain.graphql.client.query;

import graphql.language.FieldDefinition;
import graphql.language.Type;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@AllArgsConstructor
public class QueryContext {

    QueryContext parent;

    int depth;

    FieldDefinition fieldDefinition;

    Set<String> params;

    public QueryContext increment() {
        return new QueryContext(this, depth + 1, fieldDefinition, params);
    }

    public QueryContext withType(FieldDefinition fieldDefinition) {
        return new QueryContext(parent, depth, fieldDefinition, params);
    }

    public Type<?> getType() {
        return fieldDefinition.getType();
    }

}
