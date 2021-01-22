package com.jacobmountain.graphql.client.query.filters;

import com.jacobmountain.graphql.client.query.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.utils.Schema;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class FieldDuplicationFilter implements FieldFilter {

    private final Map<String, Set<String>> visited = new HashMap<>();

    @Override
    public boolean shouldAddField(QueryContext context) {
        final String path = generatePath(context);
        final Set<String> strings = visited.computeIfAbsent(path, key -> new HashSet<>());
        return strings.add(context.getFieldDefinition().getName());
    }

    private String generatePath(QueryContext context) {
        List<String> path = new ArrayList<>();
        QueryContext parent = context;
        while (parent != null) {
            path.add(0, parent.getFieldDefinition().getName());
            parent = parent.getParent();
        }
        path.set(path.size() - 1, Schema.unwrap(context.getFieldDefinition().getType()));
        return String.join(".", path);
    }

}
