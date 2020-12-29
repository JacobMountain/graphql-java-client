package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.dto.Response;
import co.uk.jacobmountain.query.QueryGenerator;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.Parameter;
import com.squareup.javapoet.*;
import graphql.language.ObjectTypeDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractQueryStage extends AbstractStage {

    private final int maxDepth;

    protected final TypeName query;

    protected final TypeName mutation;

    protected final TypeName subscription;

    public AbstractQueryStage(Schema schema, TypeMapper typeMapper, String dtoPackageName, int maxDepth) {
        super(schema, typeMapper);
        this.maxDepth = maxDepth;
        this.query = ClassName.get(dtoPackageName, schema.getQueryTypeName());
        this.mutation = schema.getMutationTypeName().map(it -> ClassName.get(dtoPackageName, it)).orElse(ClassName.get(Void.class));
        this.subscription = schema.getSubscriptionTypeName().map(it -> ClassName.get(dtoPackageName, it)).orElse(ClassName.get(Void.class));
    }

    @Override
    public List<String> getTypeArguments() {
        return Collections.singletonList("Error");
    }

    protected TypeName getReturnTypeName(MethodDetails details) {
        ObjectTypeDefinition typeDefinition = getTypeDefinition(details);
        return ParameterizedTypeName.get(ClassName.get(Response.class), typeMapper.getType(typeDefinition.getName()), TypeVariableName.get("Error"));
    }

    protected String getMethod(MethodDetails details) {
        String method = "query";
        if (details.isMutation()) {
            method = "mutate";
        } else if (details.isSubscription()) {
            method = "subscribe";
        }
        return method;
    }

    protected CodeBlock generateQueryCode(String request, MethodDetails details) {
        Set<String> params = details.getParameters()
                .stream()
                .map(Parameter::getField)
                .collect(Collectors.toSet());
        QueryGenerator queryGenerator = new QueryGenerator(schema, maxDepth);
        String query;
        if (details.isQuery()) {
            query = queryGenerator.generateQuery(request, details.getField(), params);
        } else if (details.isMutation()) {
            query = queryGenerator.generateMutation(request, details.getField(), params);
        } else if (details.isSubscription()) {
            query = queryGenerator.generateSubscription(request, details.getField(), params);
        } else {
            throw new RuntimeException("");
        }
        boolean hasArgs = details.hasParameters();
        return CodeBlock.of(
                "(\"$L\", $L)", query, hasArgs ? "args" : "null"
        );
    }

}