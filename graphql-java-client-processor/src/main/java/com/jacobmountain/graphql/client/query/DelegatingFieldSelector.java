package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.utils.Schema;
import graphql.language.TypeDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class DelegatingFieldSelector implements FieldSelector {

    private final List<FieldSelector> selectors;

    public DelegatingFieldSelector(FragmentRenderer fr, Schema schema, QueryGenerator queryGenerator) {
        this.selectors = Arrays.asList(
                fr,
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
                .reduce((a, b) -> a + " " + b)
                .map(children -> "{ " +
                        children +
                        " __typename" +
                        " }")
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}
