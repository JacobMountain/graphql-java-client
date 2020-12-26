package co.uk.jacobmountain.modules;

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

public abstract class AbstractQueryModule extends AbstractModule {

    private final Schema schema;

    private final int maxDepth;

    private final TypeMapper typeMapper;

    protected final TypeName query;

    protected final TypeName mutation;

    public AbstractQueryModule(Schema schema, int maxDepth, TypeMapper typeMapper, String dtoPackageName) {
        this.schema = schema;
        this.maxDepth = maxDepth;
        this.typeMapper = typeMapper;
        this.query = ClassName.get(dtoPackageName, schema.getQueryTypeName());
        this.mutation = schema.getMutationTypeName().map(it -> ClassName.get(dtoPackageName, it)).orElse(ClassName.get(Void.class));
    }

    @Override
    public List<String> getTypeArguments() {
        return Collections.singletonList("Error");
    }

    protected TypeName getReturnTypeName(MethodDetails details) {
        ObjectTypeDefinition query = details.isQuery() ? schema.getQuery() : schema.getMutation();
        return ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(query.getName()), TypeVariableName.get("Error"));
    }

    protected CodeBlock generateQueryCode(String request, MethodDetails details) {
        Set<String> params = details.getParameters()
                .stream()
                .map(Parameter::getField)
                .collect(Collectors.toSet());
        String query = new QueryGenerator(schema, maxDepth).generateQuery(request, details.getField(), params, details.isMutation());
        boolean hasArgs = details.hasParameters();
        return CodeBlock.of(
                String.format("(\"$L\", %s)", hasArgs ? "args" : "null"),
                query
        );
    }


}