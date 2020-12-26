package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.ReactiveFetcher;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;

public class ReactiveQueryModule extends AbstractQueryModule {

    public ReactiveQueryModule(Schema schema, int maxDepth, TypeMapper typeMapper, String dtoPackageName) {
        super(schema, maxDepth, typeMapper, dtoPackageName);
    }

    private ParameterizedTypeName generateTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(ReactiveFetcher.class),
                query,
                mutation,
                subscription,
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
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Publisher.class), getReturnTypeName(details)))
                        .add("fetcher.$L", details.isQuery() ? "query" : "mutate").add(generateQueryCode(details.getRequestName(), details))
                        .build()
        );
    }
}
