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

public class ReactiveReturnStage extends AbstractStage {

    public ReactiveReturnStage(Schema schema, TypeMapper typeMapper) {
        super(schema, typeMapper);
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = getTypeDefinition(details);
        List<CodeBlock> ret = new ArrayList<>(
                Arrays.asList(
                        CodeBlock.of("return $T.from(thing)", details.isSubscription() ? Flux.class : Mono.class),
                        CodeBlock.of("map($T::getData)", ClassName.get(Response.class)),
                        CodeBlock.of("map($T::$L)", typeMapper.getType(typeDefinition.getName()), StringUtils.camelCase("get", details.getField()))
                )
        );
        if (!returnsPublisher(details)) {
            ret.add(CodeBlock.of("blockOptional()"));
            if (!returnsOptional(details)) {
                ret.add(CodeBlock.of("orElse(null)"));
            }
        } else if (returnsClass(details, Flux.class) && !details.isSubscription()) {
            ret.add(CodeBlock.of("flatMapIterable($T.identity())", Function.class));
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

    private boolean returnsOptional(MethodDetails details) {
        return returnsClass(details, Optional.class);
    }

}
