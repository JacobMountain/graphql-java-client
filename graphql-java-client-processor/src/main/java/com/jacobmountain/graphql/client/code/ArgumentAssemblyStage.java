package com.jacobmountain.graphql.client.code;

import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.jacobmountain.graphql.client.visitor.Parameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class ArgumentAssemblyStage extends AbstractStage {

    public ArgumentAssemblyStage() {
        super(null, null);
    }

    @Override
    public Optional<CodeBlock> assemble(ClientDetails client, MethodDetails method) {
        List<Parameter> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return Optional.empty();
        }
        List<CodeBlock> ret = new ArrayList<>();
        TypeName type = ClassName.bestGuess(method.getArgumentClassname());
        ret.add(CodeBlock.of("$T args = new $T()", type, type));
        method.getParameters()
                .stream()
                .map(this::setArgumentField)
                .forEach(ret::add);
        return Optional.of(
                CodeBlock.join(ret, ";")
        );
    }

    private CodeBlock setArgumentField(Parameter param) {
        String parameter = param.getName();
        String field = param.getField();
        CodeBlock value = CodeBlock.of("$L", parameter);
        if (!param.isNullable()) {
            value = CodeBlock.of("$T.requireNonNull($L, $S)", Objects.class, parameter, String.format("%s is not nullable", parameter));
        }
        return CodeBlock.of("args.set$L($L)", StringUtils.capitalize(field), value);
    }

}
