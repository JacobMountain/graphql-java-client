package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.annotations.GraphQLField;
import com.jacobmountain.graphql.client.query.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SelectionFieldFilter implements FieldFilter {

    private final List<GraphQLField> selections;

    @Override
    public boolean shouldAddField(QueryContext context) {
        if (context.getDepth() == 2 && selections.size() > 0) {
            String field = context.getFieldDefinition().getName();
            return selections
                    .stream()
                    .map(GraphQLField::value)
                    .anyMatch(field::equals);
        }
        return true;
    }
}
