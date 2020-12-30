package com.jacobmountain.graphql.client.modules;

import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.jacobmountain.graphql.client.visitor.Parameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class ArgumentAssemblyStage extends AbstractStage {

    private final String dtoPackageName;

    public ArgumentAssemblyStage(String dtoPackageName) {
        super(null, null);
        this.dtoPackageName = dtoPackageName;
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        List<Parameter> parameters = details.getParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<CodeBlock> ret = new ArrayList<>();
        TypeName type = ClassName.get(dtoPackageName, details.getArgumentClassname());
        ret.add(CodeBlock.of("$T args = new $T()", type, type));
        details.getParameters()
                .stream()
                .map(this::setArgumentField)
                .forEach(ret::add);
        return ret;
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
