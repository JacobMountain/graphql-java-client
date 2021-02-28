package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SelectionFieldFilter implements FieldFilter {

    private final List<GraphQLFieldSelection> selections;

    @Override
    public boolean shouldAddField(QueryContext context) {
        if (context.getDepth() == 1 && selections.size() > 0) {
            String field = context.getFieldDefinition().getName();
            return selections
                    .stream()
                    .map(GraphQLFieldSelection::getValue)
                    .anyMatch(field::equals);
        }
        return true;
    }
}
