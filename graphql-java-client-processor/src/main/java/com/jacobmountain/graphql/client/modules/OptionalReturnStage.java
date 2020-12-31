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

import java.util.*;

public class OptionalReturnStage extends AbstractStage {

    public OptionalReturnStage(Schema schema, TypeMapper typeMapper) {
        super(schema, typeMapper);
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = getTypeDefinition(details);
        List<CodeBlock> ret = new ArrayList<>(
                Arrays.asList(
                        CodeBlock.of("return $T.ofNullable(thing)", Optional.class),
                        CodeBlock.of("map($T::getData)", ClassName.get(Response.class)),
                        CodeBlock.of("map($T::$L)", typeMapper.getType(typeDefinition.getName()), StringUtils.camelCase("get", details.getField()))
                )
        );

        if (!returnsOptional(details)) {
            ret.add(CodeBlock.of("orElse(null)"));
        }
        return Collections.singletonList(CodeBlock.join(ret, "\n\t."));
    }

    private boolean returnsOptional(MethodDetails details) {
        return details.getReturnType() instanceof ParameterizedTypeName &&
                ((ParameterizedTypeName) details.getReturnType()).rawType.equals(ClassName.get(Optional.class));
    }

}
