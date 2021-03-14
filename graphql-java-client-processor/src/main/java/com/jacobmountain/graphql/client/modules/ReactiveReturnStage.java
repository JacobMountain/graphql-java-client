package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import graphql.language.ObjectTypeDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactiveReturnStage extends AbstractStage {

    public static final ClassName RESPONSE_CLASS_NAME = ClassName.get(Response.class);

    public ReactiveReturnStage(Schema schema, TypeMapper typeMapper) {
        super(schema, typeMapper);
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        if (ClassName.VOID.equals(method.getReturnType())) {
            return Collections.singletonList(
                    CodeBlock.of("$T.from(thing).block()", method.isSubscription() ? Flux.class : Mono.class)
            );
        }
        ObjectTypeDefinition typeDefinition = getTypeDefinition(method);
        List<CodeBlock> ret = new ArrayList<>();
        ret.add(CodeBlock.of("return $T.from(thing)", method.isSubscription() ? Flux.class : Mono.class));
        ret.add(CodeBlock.of("map($T::getData)", RESPONSE_CLASS_NAME));
        ret.add(CodeBlock.of("filter(data -> $T.nonNull(data.$L()))",
                Objects.class,
                StringUtils.camelCase("get", method.getField()))
        );
        ret.add(CodeBlock.of("map($T::$L)",
                typeMapper.getType(typeDefinition.getName()), StringUtils.camelCase("get", method.getField()))
        );
        if (!returnsPublisher(method)) {
            ret.add(CodeBlock.of("blockOptional()"));
            if (!returnsOptional(method)) {
                ret.add(CodeBlock.of("orElse(null)"));
            }
        } else if (returnsClass(method, Flux.class) && !method.isSubscription()) {
            ret.add(CodeBlock.of("flatMapIterable($T.identity())", Function.class));
        } else if (returnsClass(method, Mono.class, Void.class)) {
            ret.add(CodeBlock.of("then()"));
        }
        return Collections.singletonList(CodeBlock.join(ret, "\n\t."));
    }

    private boolean returnsPublisher(MethodDetails details) {
        return returnsClass(details, Mono.class) || returnsClass(details, Flux.class);
    }

    private boolean returnsClass(MethodDetails details, Class<?> clazz) {
        return details.getReturnType() instanceof ParameterizedTypeName &&
                ((ParameterizedTypeName) details.getReturnType()).rawType.equals(ClassName.get(clazz));
    }

    private boolean returnsClass(MethodDetails details, Class<?> clazz, Class<?>... nested) {
        return returnsClass(details, clazz) &&
                ((ParameterizedTypeName) details.getReturnType()).typeArguments.equals(
                        Stream.of(nested)
                                .map(ClassName::get)
                                .collect(Collectors.toList())
                );
    }

    private boolean returnsOptional(MethodDetails details) {
        return returnsClass(details, Optional.class);
    }

}
