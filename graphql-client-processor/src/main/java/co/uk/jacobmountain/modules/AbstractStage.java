package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import graphql.language.ObjectTypeDefinition;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractStage {

    protected final Schema schema;

    protected final TypeMapper typeMapper;

    public List<MemberVariable> getMemberVariables() {
        return Collections.emptyList();
    }

    public List<String> getTypeArguments() {
        return Collections.emptyList();
    }

    public List<CodeBlock> assemble(MethodDetails details) {
        return Collections.emptyList();
    }

    protected ObjectTypeDefinition getTypeDefinition(MethodDetails details) {
        if (details.isQuery()) {
            return schema.getQuery();
        } else if (details.isMutation()) {
            return schema.getMutation();
        } else {
            return schema.getSubscription();
        }
    }

    @Value
    @Builder
    public static class MemberVariable {

        String name;

        TypeName type;

    }

}