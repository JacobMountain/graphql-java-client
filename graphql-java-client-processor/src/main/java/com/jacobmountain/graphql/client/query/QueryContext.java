package com.jacobmountain.graphql.client.query;

import graphql.language.FieldDefinition;
import graphql.language.Type;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
@AllArgsConstructor
public class QueryContext {

    QueryContext parent;

    Type<?> type;

    int depth;

    FieldDefinition fieldDefinition;

    Set<String> params;

    public QueryContext increment() {
        return new QueryContext(this, type, depth + 1, fieldDefinition, params);
    }

    public QueryContext withType(FieldDefinition fieldDefinition) {
        return new QueryContext(parent, this.fieldDefinition.getType(), depth, fieldDefinition, params);
    }

}
