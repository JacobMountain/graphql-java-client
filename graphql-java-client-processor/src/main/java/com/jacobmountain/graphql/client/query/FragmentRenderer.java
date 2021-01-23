package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.annotations.GraphQLField;
import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import com.jacobmountain.graphql.client.query.filters.SelectionFieldFilter;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import graphql.com.google.common.collect.Sets;
import graphql.language.Selection;
import graphql.language.TypeDefinition;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FragmentRenderer implements FieldSelector {

    private final Schema schema;

    private final QueryGenerator queryGenerator;

    private final Map<String, GraphQLFragment> fragments;

    private final Map<String, String> generated = new HashMap<>();

    public FragmentRenderer(Schema schema, QueryGenerator queryGenerator, List<GraphQLFragment> fragments) {
        this.schema = schema;
        this.queryGenerator = queryGenerator;
        this.fragments = fragments.stream()
                .collect(Collectors.toMap(GraphQLFragment::type, Function.identity()));
    }

    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition, QueryContext context, FragmentRenderer fragmentRenderer, Set<String> argumentCollector, List<FieldFilter> filters) {
        final String type = Schema.unwrap(context.getFieldDefinition().getType());
        final GraphQLFragment graphQLFragment = fragments.get(type);
        if (graphQLFragment == null || !type.equals(graphQLFragment.type())) {
            return Stream.empty();
        }
        final Set<String> select = Stream.of(graphQLFragment.select())
                .map(GraphQLField::value)
                .collect(Collectors.toSet());
        final List<String> collect = schema.getChildren(typeDefinition)
                .filter(field -> select.contains(field.getName()))
                .map(definition -> queryGenerator.generateFieldSelection(
                        definition.getName(),
                        context.withType(definition).increment(),
                        this,
                        argumentCollector,
                        filters
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        final String name = getFragmentName(graphQLFragment);
        generated.computeIfAbsent(graphQLFragment.type(), a -> "fragment " + name + " on " + graphQLFragment.type() + " { " + String.join(" ", collect) + " }");
        if (collect.size() > 0) {
            return Stream.of("..." + name);
        }
        return Stream.empty();
    }

    private String getFragmentName(GraphQLFragment fragment) {
        return StringUtils.camelCase(fragment.type());
    }

    public String render() {
        return String.join(" ", generated.values());
    }


}
