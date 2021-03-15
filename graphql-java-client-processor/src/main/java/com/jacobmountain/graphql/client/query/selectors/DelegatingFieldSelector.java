package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import graphql.language.TypeDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DelegatingFieldSelector implements FieldSelector {

    private final List<FieldSelector> selectors;

    public DelegatingFieldSelector(FieldSelector... selectors) {
        this.selectors = Arrays.asList(selectors);
    }

    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition,
                                       QueryContext context,
                                       List<FieldFilter> filters) {
        return selectors.stream()
                .flatMap(selector -> selector.selectFields(typeDefinition, context, filters))
                .reduce((a, b) -> String.join(" ", a, b))
                .map(children -> "{ " +
                        children +
                        " __typename " +
                        "}"
                )
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
