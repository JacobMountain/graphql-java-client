package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.Fetcher;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.Collections;
import java.util.List;

public class BlockingQueryModule extends AbstractQueryModule {

    public BlockingQueryModule(Schema schema, String dtoPackageName, int maxDepth, TypeMapper typeMapper) {
        super(schema, maxDepth, typeMapper, dtoPackageName);
    }

    private ParameterizedTypeName generateTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(Fetcher.class),
                query,
                mutation,
                TypeVariableName.get("Error")
        );
    }

    @Override
    public List<MemberVariable> getMemberVariables() {
        return Collections.singletonList(
                MemberVariable.builder()
                        .name("fetcher")
                        .type(generateTypeName())
                        .build()
        );
    }

    @Override
    public boolean handlesAssembly(MethodDetails details) {
        return details.isQuery() || details.isMutation();
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", getReturnTypeName(details))
                        .add("fetcher.$L", details.isQuery() ? "query" : "mutate").add(generateQueryCode(details.getRequestName(), details))
                        .build()
        );
    }

}
