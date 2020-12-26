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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class ReactiveReturnModule extends AbstractStage {

    private final Schema schema;

    private final TypeMapper typeMapper;

    @Override
    public boolean handlesAssembly(MethodDetails details) {
        return true;
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = details.isQuery() ? schema.getQuery() : schema.getMutation();
        List<CodeBlock> ret = new ArrayList<>(
                Arrays.asList(
                        CodeBlock.of("return $T.from(thing)", Mono.class),
                        CodeBlock.of("map($T::getData)", ClassName.get(Response.class)),
                        CodeBlock.of("map($T::$L)", typeMapper.getType(typeDefinition.getName()), StringUtils.camelCase("get", details.getField()))
                )
        );
        if (!returnsPublisher(details)) {
            ret.add(CodeBlock.of("blockOptional()"));
            if (!returnsOptional(details)) {
                ret.add(CodeBlock.of("orElse(null)"));
            }
        } else {
            if (returnsClass(details, Flux.class)) {
                ret.add(CodeBlock.of("flatMapIterable($T.identity())", Function.class));
            }
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
