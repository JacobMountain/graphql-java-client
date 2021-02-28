package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import com.jacobmountain.graphql.client.query.QueryContext;
import graphql.language.TypeDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface FieldSelector {

    Stream<String> selectFields(TypeDefinition<?> typeDefinition,
                                QueryContext context,
                                Set<String> argumentCollector,
                                List<FieldFilter> filters);

}
