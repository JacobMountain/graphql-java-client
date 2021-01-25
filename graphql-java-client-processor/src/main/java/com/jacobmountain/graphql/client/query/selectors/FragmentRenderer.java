package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection;
import graphql.language.FieldDefinition;
import graphql.language.TypeDefinition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FragmentRenderer implements FieldSelector {

    private final Schema schema;

    private final QueryGenerator queryGenerator;

    private final Map<String, Fragment> fragments;

    static class Fragment {

        String type;

        String name;

        Set<GraphQLFieldSelection> selection;

        public Fragment(GraphQLFragment annotation) {
            this.type = annotation.type();
            this.name = Optional.of(annotation.name())
                    .filter(StringUtils::hasLength)
                    .orElseGet(() -> StringUtils.camelCase(annotation.type()));
            this.selection = Stream.of(annotation.select())
                    .map(GraphQLFieldSelection::new)
                    .collect(Collectors.toSet());
        }
    }

    private final Map<String, String> generated = new HashMap<>();

    public FragmentRenderer(Schema schema, QueryGenerator queryGenerator, List<GraphQLFragment> fragments) {
        this.schema = schema;
        this.queryGenerator = queryGenerator;
        this.fragments = fragments.stream()
                .collect(Collectors.toMap(GraphQLFragment::type, Fragment::new));
    }

    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition, QueryContext context, Set<String> argumentCollector, List<FieldFilter> filters) {
        final String type = Schema.unwrap(context.getFieldDefinition().getType());
        final Fragment fragment = fragments.get(type);
        if (fragment == null) {
            return Stream.empty();
        }
        final Optional<String> children = new DelegatingFieldSelector(schema, queryGenerator)
                .selectFields(typeDefinition, context, argumentCollector, filters)
                .findFirst();
        generated.computeIfAbsent(fragment.type, a -> "fragment " + fragment.name + " on " + fragment.type + " " + children.orElse(""));
        if (children.isPresent()) {
            return Stream.of("..." + fragment.name);
        }
        return Stream.empty();
    }

    public String render() {
        return String.join(" ", generated.values());
    }

}
