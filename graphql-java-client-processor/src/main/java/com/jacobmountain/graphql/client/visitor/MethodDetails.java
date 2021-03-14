package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.annotations.GraphQLField;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class MethodDetails {

    @Getter
    private final String methodName;

    // The name of the GraphQL request or empty
    @Getter
    private final String requestName;

    @Getter
    private final TypeName returnType;

    @Getter
    private final String field;

    @Getter
    @Singular
    private final List<Parameter> parameters;

    @Getter
    private final List<GraphQLField> selection;

    private final boolean mutation;

    private final boolean subscription;

    @Getter
    private final int maxDepth;

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public List<ParameterSpec> getParameterSpec() {
        return parameters.stream()
                .map(Parameter::toSpec)
                .collect(Collectors.toList());
    }

    public boolean isQuery() {
        return !(mutation || subscription);
    }

    public boolean isMutation() {
        return mutation;
    }

    public boolean isSubscription() {
        return subscription;
    }

    @Override
    public String toString() {
        String arguments = parameters.stream()
                .map(param -> MessageFormat.format("{0} {1}", getTypeString(param.getType()), param.getName()))
                .collect(Collectors.joining(", "));
        return MessageFormat.format("{0} {1}({2})", getTypeString(returnType), methodName, arguments);
    }

    private String getName() {
        return Optional.ofNullable(requestName).filter(StringUtils::hasLength).orElse(methodName);
    }

    public String getArgumentClassname() {
        return StringUtils.pascalCase(getName(), "By") + parameters.stream()
                .map(Parameter::getName)
                .map(StringUtils::pascalCase)
                .collect(Collectors.joining("And"));
    }

    public static String getTypeString(TypeName type) {
        if (type instanceof ClassName) {
            return ((ClassName) type).simpleName();
        } else if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) type;
            String raw = parameterizedTypeName.rawType.simpleName();
            String collect = parameterizedTypeName.typeArguments.stream()
                    .map(MethodDetails::getTypeString)
                    .collect(Collectors.joining(", "));
            return String.format("%s<%s>", raw, collect);
        } else {
            return type.toString();
        }
    }

    public boolean returnsClass(Class<?> clazz, Class<?>... nested) {
        if (returnType instanceof ParameterizedTypeName) {
            final ParameterizedTypeName returnType = (ParameterizedTypeName) this.returnType;
            return returnType.rawType.equals(ClassName.get(clazz)) &&
                    Stream.of(nested)
                            .map(ClassName::get)
                            .allMatch(returnType.typeArguments::contains);
        }
        return returnType.equals(ClassName.get(clazz)) && nested.length == 0;
    }

}
