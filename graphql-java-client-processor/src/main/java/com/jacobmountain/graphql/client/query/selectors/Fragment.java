package com.jacobmountain.graphql.client.query.selectors;

import com.jacobmountain.graphql.client.annotations.GraphQLFragment;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class Fragment {

    private String type;

    private String name;

    private Set<GraphQLFieldSelection> selection;

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
