package com.jacobmountain.graphql.client.visitor;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.annotations.GraphQLArgument;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;
import com.jacobmountain.graphql.client.annotations.GraphQLSubscription;
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
        return getQueryDetails(e, typeMapper)
                .orElseGet(() -> getSubscriptionDetails(e, typeMapper).orElse(null));
    }

    private Optional<MethodDetails> getQueryDetails(ExecutableElement e, TypeMapper typeMapper) {
        return Optional.ofNullable(e.getAnnotation(GraphQLQuery.class))
                .map(annotation -> MethodDetails.builder()
                        .methodName(e.getSimpleName().toString())
                        .requestName(annotation.request())
                        .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                        .field(annotation.value())
                        .mutation(annotation.mutation())
                        .subscription(false)
                        .parameters(
                                e.getParameters()
                                        .stream()
                                        .map(parameter -> {
                                                    String className = parameter.getSimpleName().toString();
                                                    return Parameter.builder()
                                                            .type(typeMapper.defaultPackage(ClassName.get(parameter.asType())))
                                                            .name(className)
                                                            .annotation(parameter.getAnnotation(GraphQLArgument.class))
                                                            .nullable(
                                                                    isNullableArg(annotation.value(), className)
                                                            )
                                                            .build();
                                                }
                                        )
                                        .collect(Collectors.toList())
                        )
                        .build());
    }

    private Optional<MethodDetails> getSubscriptionDetails(ExecutableElement e, TypeMapper typeMapper) {
        return Optional.ofNullable(e.getAnnotation(GraphQLSubscription.class))
                .map(annotation -> MethodDetails.builder()
                        .methodName(e.getSimpleName().toString())
                        .requestName(annotation.request())
                        .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                        .field(annotation.value())
                        .mutation(false)
                        .subscription(true)
                        .parameters(
                                e.getParameters()
                                        .stream()
                                        .map(parameter -> {
                                                    String className = parameter.getSimpleName().toString();
                                                    return Parameter.builder()
                                                            .type(typeMapper.defaultPackage(ClassName.get(parameter.asType())))
                                                            .name(className)
                                                            .annotation(parameter.getAnnotation(GraphQLArgument.class))
                                                            .nullable(
                                                                    isNullableArg(annotation.value(), className)
                                                            )
                                                            .build();
                                                }
                                        )
                                        .collect(Collectors.toList())
                        )
                        .build());
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
