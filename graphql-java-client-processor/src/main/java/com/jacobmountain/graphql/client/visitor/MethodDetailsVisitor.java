package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.annotations.GraphQLArgument;
import com.jacobmountain.graphql.client.annotations.GraphQLMutation;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;
import com.jacobmountain.graphql.client.annotations.GraphQLSubscription;
import com.jacobmountain.graphql.client.exceptions.MissingAnnotationException;
import com.jacobmountain.graphql.client.utils.OptionalUtils;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MethodDetailsVisitor extends ElementKindVisitor8<MethodDetails, TypeMapper> {

    private final Schema schema;

    public MethodDetailsVisitor(Schema registry) {
        this.schema = registry;
    }

    @Override
    public MethodDetails visitExecutableAsMethod(ExecutableElement e, TypeMapper typeMapper) {
        return OptionalUtils.first(
                getQueryDetails(e, typeMapper),
                () -> getMutationDetails(e, typeMapper),
                () -> getSubscriptionDetails(e, typeMapper)
        ).orElseThrow(() -> new MissingAnnotationException("@GraphQLClient methods require @GraphQLQuery, @GraphQLMutation, or @GraphQLSubscription annotations"));
    }

    private Optional<MethodDetails> getQueryDetails(ExecutableElement e, TypeMapper typeMapper) {
        return Optional.ofNullable(e.getAnnotation(GraphQLQuery.class))
                .map(annotation -> MethodDetails.builder()
                        .methodName(e.getSimpleName().toString())
                        .requestName(annotation.name())
                        .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                        .field(annotation.value())
                        .mutation(false)
                        .subscription(false)
                        .maxDepth(annotation.maxDepth())
                        .parameters(getParameters(e, typeMapper, annotation.value()))
                        .selection(Arrays.asList(annotation.select()))
                        .build());
    }

    private Optional<MethodDetails> getMutationDetails(ExecutableElement e, TypeMapper typeMapper) {
        return Optional.ofNullable(e.getAnnotation(GraphQLMutation.class))
                .map(annotation -> MethodDetails.builder()
                        .methodName(e.getSimpleName().toString())
                        .requestName(annotation.name())
                        .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                        .field(annotation.value())
                        .mutation(true)
                        .subscription(false)
                        .maxDepth(annotation.maxDepth())
                        .parameters(getParameters(e, typeMapper, annotation.value()))
                        .selection(Arrays.asList(annotation.select()))
                        .build());
    }

    private Optional<MethodDetails> getSubscriptionDetails(ExecutableElement e, TypeMapper typeMapper) {
        return Optional.ofNullable(e.getAnnotation(GraphQLSubscription.class))
                .map(annotation -> MethodDetails.builder()
                        .methodName(e.getSimpleName().toString())
                        .requestName(annotation.name())
                        .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                        .field(annotation.value())
                        .mutation(false)
                        .subscription(true)
                        .maxDepth(annotation.maxDepth())
                        .parameters(getParameters(e, typeMapper, annotation.value()))
                        .selection(Arrays.asList(annotation.select()))
                        .build()
                );
    }

    private List<Parameter> getParameters(ExecutableElement e, TypeMapper typeMapper, String root) {
        return e.getParameters()
                .stream()
                .map(parameter -> {
                            String className = parameter.getSimpleName().toString();
                            return Parameter.builder()
                                    .type(typeMapper.defaultPackage(ClassName.get(parameter.asType())))
                                    .name(className)
                                    .annotation(parameter.getAnnotation(GraphQLArgument.class))
                                    .nullable(isNullableArg(root, className))
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }

    private boolean isNullableArg(String field, String arg) {
        if (schema == null) {
            return true;
        }
        Optional<FieldDefinition> type = schema.findField(field);
        if (!type.isPresent()) {
            return true;
        }
        InputValueDefinition definition = type.get()
                .getInputValueDefinitions()
                .stream()
                .filter(it -> StringUtils.equals(it.getName(), arg))
                .findFirst()
                .orElse(null);
        return definition == null || !definition.getType().getClass().equals(NonNullType.class);
    }

}
