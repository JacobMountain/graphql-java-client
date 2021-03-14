package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.dto.Response;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import graphql.language.ObjectTypeDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class OptionalReturnStage extends AbstractStage {

    private static final ClassName RESPONSE_CLASS_NAME = ClassName.get(Response.class);

    public OptionalReturnStage(Schema schema, TypeMapper typeMapper) {
        super(schema, typeMapper);
    }

    @Override
    public List<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        if (ClassName.VOID.equals(method.getReturnType())) {
            if (!method.isMutation()) {
                throw new IllegalArgumentException("void return type on a non mutation method");
            }
            return Collections.emptyList();
        }
        ObjectTypeDefinition typeDefinition = getTypeDefinition(method);
        List<CodeBlock> ret = new ArrayList<>();
        ret.add(CodeBlock.of("return $T.ofNullable(thing)", Optional.class));
        ret.add(CodeBlock.of("map($T::getData)", RESPONSE_CLASS_NAME));
        ret.add(CodeBlock.of("map($T::$L)",
                typeMapper.getType(typeDefinition.getName()), StringUtils.camelCase("get", method.getField()))
        );
        if (!method.returnsClass(Optional.class)) {
            ret.add(CodeBlock.of("orElse(null)"));
        }
        return Collections.singletonList(CodeBlock.join(ret, "\n\t."));
    }

}
