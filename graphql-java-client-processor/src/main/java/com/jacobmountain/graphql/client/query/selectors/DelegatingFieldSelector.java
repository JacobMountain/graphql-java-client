package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import com.jacobmountain.graphql.client.utils.Schema;
import graphql.language.TypeDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class DelegatingFieldSelector implements FieldSelector {

    private final List<FieldSelector> selectors;

    public DelegatingFieldSelector(FragmentRenderer fragmentRenderer, Schema schema, QueryGenerator queryGenerator) {
        this.selectors = Arrays.asList(
                fragmentRenderer,
                new DefaultFieldSelector(schema, queryGenerator),
                new InlineFragmentRenderer(schema, queryGenerator)
        );
    }

    public DelegatingFieldSelector(Schema schema, QueryGenerator queryGenerator) {
        this.selectors = Arrays.asList(
                new DefaultFieldSelector(schema, queryGenerator),
                new InlineFragmentRenderer(schema, queryGenerator)
        );
    }



    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition,
                                       QueryContext context,
                                       Set<String> argumentCollector,
                                       List<FieldFilter> filters) {
        return selectors.stream()
                .flatMap(selector -> selector.selectFields(typeDefinition, context, argumentCollector, filters))
                .reduce((a, b) -> String.join(" ", a, b))
                .map(children -> "{ " +
                        children +
                        " __typename" +
                        " }")
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
