package com.jacobmountain.graphql.client.query;

import graphql.language.TypeDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface FieldSelector {

    Stream<String> selectFields(TypeDefinition<?> typeDefinition,
                                QueryContext context,
                                FragmentRenderer fragmentRenderer,
                                Set<String> argumentCollector,
                                List<FieldFilter> filters);

}
