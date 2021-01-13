package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.query.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import graphql.language.NonNullType;

/**
 * Adds the field to the query if all of its non null arguments, are available on the method
 */
public class AllNonNullArgsFieldFilter implements FieldFilter {

    @Override
    public boolean shouldAddField(QueryContext context) {
        return context.getFieldDefinition()
                .getInputValueDefinitions()
                .stream()
                .filter(input -> input.getType() instanceof NonNullType)
                .allMatch(nonNull -> context.getParams().contains(nonNull.getName()));
    }

}
