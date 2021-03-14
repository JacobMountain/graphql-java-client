package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import graphql.language.ObjectTypeDefinition;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

public class ReactiveReturnStage extends AbstractStage {

    public static final ClassName RESPONSE_CLASS_NAME = ClassName.get(Response.class);

    public ReactiveReturnStage(Schema schema, TypeMapper typeMapper) {
        super(schema, typeMapper);
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        if (ClassName.VOID.equals(method.getReturnType())) {
            if (!method.isMutation()) {
                throw new IllegalArgumentException("void return type on a non mutation method");
            }
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

        unwrapReturnType(method).ifPresent(ret::add);

        return Collections.singletonList(CodeBlock.join(ret, "\n\t."));
    }

    private Optional<CodeBlock> unwrapReturnType(MethodDetails method) {
        if (method.returnsClass(Mono.class, Void.class)) {
            return Optional.of(CodeBlock.of("then()"));
        } else if (method.returnsClass(Flux.class) && !method.isSubscription()) {
            return Optional.of(CodeBlock.of("flatMapIterable($T.identity())", Function.class));
        } else if (method.returnsClass(Optional.class)) {
            return Optional.of(CodeBlock.of("blockOptional()"));
        } else if (method.returnsClass(Mono.class) || method.returnsClass(Flux.class) || method.returnsClass(Publisher.class)) {
            return Optional.empty();
        } else {
            return Optional.of(CodeBlock.of("block()"));
        }
    }

}
