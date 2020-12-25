package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.Fetcher;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.*;

import java.util.Collections;
import java.util.List;

public class BlockingQueryModule extends AbstractQueryModule {

    protected final String dtoPackageName;

    protected final TypeName query;

    protected final TypeName mutation;

    public BlockingQueryModule(Schema schema, String dtoPackageName, int maxDepth, TypeMapper typeMapper) {
        super(schema, maxDepth, typeMapper);
        this.dtoPackageName = dtoPackageName;
        this.query = ClassName.get(this.dtoPackageName, schema.getQueryTypeName());
        this.mutation = schema.getMutationTypeName().map(it -> ClassName.get(this.dtoPackageName, it)).orElse(ClassName.get(Void.class));
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
        return assembleFetchAndReturn(details);
    }

    private List<CodeBlock> assembleFetchAndReturn(MethodDetails details) {
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", getReturnTypeName(details))
                        .add("fetcher.$L", details.isQuery() ? "query" : "mutate").add(generateQueryCode(details.getRequestName(), details))
                        .build()
        );
    }

}
