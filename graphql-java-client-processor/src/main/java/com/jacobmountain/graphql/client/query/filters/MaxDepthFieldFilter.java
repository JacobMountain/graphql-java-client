package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.query.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MaxDepthFieldFilter implements FieldFilter {

    private final int maxDepth;

    @Override
    public boolean shouldAddField(QueryContext context) {
        return context.getDepth() < maxDepth;
    }

}
