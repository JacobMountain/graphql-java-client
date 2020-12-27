package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.Fetcher;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.dto.Response;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import graphql.language.ObjectTypeDefinition;

import java.util.Collections;
import java.util.List;

public class BlockingQueryStage extends AbstractQueryStage {

    public BlockingQueryStage(Schema schema, TypeMapper typeMapper, String dtoPackageName, int maxDepth) {
        super(schema, typeMapper, dtoPackageName, maxDepth);
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
    public List<String> getTypeArguments() {
        return Collections.singletonList("Error");
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        ObjectTypeDefinition query = getTypeDefinition(details);
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(query.getName()), TypeVariableName.get("Error")))
                        .add("fetcher.$L", getMethod(details)).add(generateQueryCode(details.getRequestName(), details))
                        .build()
        );
    }

}