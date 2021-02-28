package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.query.QueryContext;

@FunctionalInterface
public interface FieldFilter {

    boolean shouldAddField(QueryContext context);

}
