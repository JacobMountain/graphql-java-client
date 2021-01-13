package com.jacobmountain.graphql.client.query;

@FunctionalInterface
public interface FieldFilter {

    boolean shouldAddField(QueryContext context);

}
