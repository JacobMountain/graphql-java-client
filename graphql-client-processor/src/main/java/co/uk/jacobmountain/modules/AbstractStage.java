package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.List;

public abstract class AbstractStage {

    public List<MemberVariable> getMemberVariables() {
        return Collections.emptyList();
    }

    public List<String> getTypeArguments() {
        return Collections.emptyList();
    }

    public boolean handlesAssembly(MethodDetails details) {
        return false;
    }

    public List<CodeBlock> assemble(MethodDetails details) {
        return Collections.emptyList();
    }

    @Value
    @Builder
    public static class MemberVariable {

        String name;

        TypeName type;

    }

}