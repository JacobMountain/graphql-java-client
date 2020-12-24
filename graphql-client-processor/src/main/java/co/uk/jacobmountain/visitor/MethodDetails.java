package co.uk.jacobmountain.visitor;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class MethodDetails {

    @Getter
    private final String name;

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

}
