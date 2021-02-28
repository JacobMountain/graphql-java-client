package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.query.QueryContext;
import com.jacobmountain.graphql.client.query.filters.FieldFilter;
import graphql.language.TypeDefinition;

import java.util.List;
import java.util.stream.Stream;

public interface FieldSelector {

    Stream<String> selectFields(TypeDefinition<?> typeDefinition,
                                QueryContext context,
                                List<FieldFilter> filters);

}
