package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.dto.Response;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import graphql.language.ObjectTypeDefinition;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class OptionalReturnStage extends AbstractStage {

    private final Schema schema;

    private final TypeMapper typeMapper;

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = details.isQuery() ? schema.getQuery() : schema.getMutation();
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
