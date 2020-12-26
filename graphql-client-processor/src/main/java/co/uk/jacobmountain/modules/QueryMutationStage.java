package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.Fetcher;
import co.uk.jacobmountain.QueryGenerator;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.dto.Response;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.Parameter;
import com.squareup.javapoet.*;
import graphql.language.ObjectTypeDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryMutationStage extends AbstractStage {

    private final Schema schema;

    private final String dtoPackageName;

    private final int maxDepth;

    private final TypeMapper typeMapper;

    private final TypeName query;

    private final TypeName mutation;

    public QueryMutationStage(Schema schema, String dtoPackageName, int maxDepth, TypeMapper typeMapper) {
        this.schema = schema;
        this.dtoPackageName = dtoPackageName;
        this.maxDepth = maxDepth;
        this.typeMapper = typeMapper;
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
    public List<String> getTypeArguments() {
        return Collections.singletonList("Error");
    }

    @Override
    public boolean handlesAssembly(MethodDetails details) {
        return details.isQuery() || details.isMutation();
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        return assembleFetchAndReturn(details);
    }

    private CodeBlock generateQueryCode(String request, MethodDetails details) {
        Set<String> params = details.getParameters()
                .stream()
                .map(Parameter::getField)
                .collect(Collectors.toSet());
        String query = new QueryGenerator(schema, maxDepth).generateQuery(request, details.getField(), params, details.isMutation());
        boolean hasArgs = details.hasParameters();
        return CodeBlock.of(
                String.format(".%s(\"$L\", %s)", details.isQuery() ? "query" : "mutate", hasArgs ? "args" : "null"),
                query
        );
    }

    private List<CodeBlock> assembleFetchAndReturn(MethodDetails details) {
        CodeBlock.Builder builder = CodeBlock.builder();
        ObjectTypeDefinition query = details.isQuery() ? schema.getQuery() : schema.getMutation();
        builder.add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(query.getName()), TypeVariableName.get("Error")));
        builder.add("fetcher")
                .add(generateQueryCode(details.getRequestName(), details));
        return Collections.singletonList(builder.build());
    }


}