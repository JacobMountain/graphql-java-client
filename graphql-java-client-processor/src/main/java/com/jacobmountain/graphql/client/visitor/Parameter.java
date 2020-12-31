package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.annotations.GraphQLArgument;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parameter {

    private TypeName type;

    private String name;

    private GraphQLArgument annotation;

    private boolean nullable;

    public ParameterSpec toSpec() {
        return ParameterSpec.builder(type, name).build();
    }

    public String getField() {
        return annotation == null ? name : annotation.value();
    }

    public GraphQLArgument getAnnotation() {
        return annotation;
    }

}
