package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.annotations.GraphQLField;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GraphQLFieldSelection {

    private String value;

    public GraphQLFieldSelection(GraphQLField annotation) {
        this.value = annotation.value();
    }

}
