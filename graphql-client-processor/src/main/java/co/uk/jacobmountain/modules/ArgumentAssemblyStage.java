package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.utils.StringUtils;
import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.Parameter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static co.uk.jacobmountain.ClientGenerator.generateArgumentClassname;

@RequiredArgsConstructor
public class ArgumentAssemblyStage extends AbstractStage {

    private final String dtoPackageName;

    @Override
    public boolean handlesAssembly(MethodDetails details) {
        return true;
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        List<Parameter> parameters = details.getParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<CodeBlock> ret = new ArrayList<>();
        TypeName type = ClassName.get(dtoPackageName, generateArgumentClassname(details));
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
