package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.utils.Schema;
import graphql.language.FieldDefinition;
import graphql.language.TypeDefinition;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultFieldSelector implements FieldSelector {

    private final Schema schema;

    private final QueryGenerator queryGenerator;

    @Override
    public Stream<String> selectFields(TypeDefinition<?> typeDefinition, QueryContext context, Set<String> argumentCollector, List<FieldFilter> filters) {
        return schema.getChildren(typeDefinition)
                .filter(this::filter)
                .map(definition -> queryGenerator.generateFieldSelection(
                        definition.getName(),
                        context.withType(definition).increment(),
                        argumentCollector,
                        filters
                ))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    protected boolean filter(FieldDefinition fieldDefinition) {
        return true;
    }

}
