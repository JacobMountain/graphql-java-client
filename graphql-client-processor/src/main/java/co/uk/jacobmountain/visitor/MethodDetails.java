package co.uk.jacobmountain.visitor;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
public class MethodDetails {

    private final TypeName returnType;

    private final String field;

    @Singular
    private final List<ParameterSpec> parameters;

    private final boolean mutation;

    public TypeName getReturnType() {
        return returnType;
    }

    public String getField() {
        return field;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public List<ParameterSpec> getParameters() {
        return parameters;
    }

    public boolean isQuery() {
        return !mutation;
    }

    public boolean isMutation() {
        return mutation;
    }

}
