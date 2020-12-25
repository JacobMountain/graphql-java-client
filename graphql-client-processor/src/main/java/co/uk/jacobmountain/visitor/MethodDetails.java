package co.uk.jacobmountain.visitor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class MethodDetails {

    @Getter
    private final String methodName;

    // The name of the GraphQL request or empty
    @Getter
    private final String requestName;

    private final TypeName returnType;

    @Getter
    private final String field;

    @Singular
    private final List<Parameter> parameters;

    private final boolean mutation;

    public TypeName getReturnType() {
        return returnType;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<ParameterSpec> getParameterSpec() {
        return parameters.stream()
                .map(Parameter::toSpec)
                .collect(Collectors.toList());
    }

    public boolean isQuery() {
        return !mutation;
    }

    public boolean isMutation() {
        return mutation;
    }

    @Override
    public String toString() {
        String arguments = parameters.stream()
                .map(param -> MessageFormat.format("{0} {1}", getTypeString(param.getType()), param.getName()))
                .collect(Collectors.joining(", "));
        return MessageFormat.format("{0} {1}({2})", getTypeString(returnType), methodName, arguments);
    }

    public static String getTypeString(TypeName type) {
        if (type instanceof ClassName) {
            return ((ClassName) type).simpleName();
        } else if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) type;
            String raw = parameterizedTypeName.rawType.simpleName();
            String collect = parameterizedTypeName.typeArguments.stream()
                    .map(MethodDetails::getTypeString)
                    .collect(Collectors.joining(", "));
            return String.format("%s<%s>", raw, collect);
        } else {
            return type.toString();
        }
    }

}
