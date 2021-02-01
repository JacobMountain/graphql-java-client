package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.Schema;
import graphql.language.TypeDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FragmentRenderer implements FieldSelector {

    private final Schema schema;

    private final QueryGenerator queryGenerator;

    private final Map<String, Fragment> fragments;

    private final Map<String, String> generated = new HashMap<>();

    public FragmentRenderer(Schema schema, QueryGenerator queryGenerator, List<Fragment> fragments) {
        this.schema = schema;
        this.queryGenerator = queryGenerator;
        this.fragments = fragments.stream()
                .collect(Collectors.toMap(Fragment::getType, Function.identity(), FragmentRenderer::duplicateFragments));
    }

    private static Fragment duplicateFragments(Fragment fragment, Fragment fragment2) {
        if (!fragment.equals(fragment2)) {
            log.warn("Duplicate fragments defined {} {}", fragment.getName(), fragment2.getName());
        }
        return fragment;
    }

    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition, QueryContext context, Set<String> argumentCollector, List<FieldFilter> filters) {
        log.info("\nGenerating fragment");
        final String type = Schema.unwrap(context.getFieldDefinition().getType());
        final Fragment fragment = fragments.get(type);
        if (fragment == null) {
            return Stream.empty();
        }
        final Optional<String> children = new DelegatingFieldSelector(schema, queryGenerator)
                .selectFields(schema.getTypeDefinition(fragment.getType()).orElseThrow(RuntimeException::new), context, argumentCollector, filters)
                .findFirst();
        if (children.isPresent()) {
            generated.computeIfAbsent(fragment.getType(), a -> "fragment " + fragment.getName() + " on " + fragment.getName() + " " + children.get());
            return Stream.of("..." + fragment.getName());
        }
        return Stream.empty();
    }

    public String render() {
        return String.join(" ", generated.values());
    }

}
