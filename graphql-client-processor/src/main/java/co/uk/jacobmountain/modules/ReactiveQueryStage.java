package co.uk.jacobmountain.modules;

import co.uk.jacobmountain.ReactiveFetcher;
import co.uk.jacobmountain.ReactiveSubscriber;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.visitor.MethodDetails;
import com.squareup.javapoet.*;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactiveQueryStage extends AbstractQueryStage {

    public ReactiveQueryStage(Schema schema, TypeMapper typeMapper, String dtoPackageName, int maxDepth) {
        super(schema, typeMapper, dtoPackageName, maxDepth);
    }

    private TypeName getFetcherTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(ReactiveFetcher.class),
                query,
                mutation,
                TypeVariableName.get("Error")
        );
    }

    private TypeName getSubscriberTypeName() {
        return ParameterizedTypeName.get(
                ClassName.get(ReactiveSubscriber.class),
                subscription,
                TypeVariableName.get("Error")
        );
    }

    @Override
    public List<MemberVariable> getMemberVariables(ClientDetails details) {
        ArrayList<MemberVariable> vars = new ArrayList<>();
        if (details.requiresFetcher()) {
            vars.add(
                    MemberVariable.builder()
                            .name("fetcher")
                            .type(getFetcherTypeName())
                            .build()
            );
        }
        if (details.requiresSubscriber()) {
            vars.add(
                    MemberVariable.builder()
                            .name("subscriber")
                            .type(getSubscriberTypeName())
                            .build()
            );
        }
        return vars;
    }

    @Override
    public List<CodeBlock> assemble(MethodDetails details) {
        String member = details.isSubscription() ? "subscriber" : "fetcher";
        return Collections.singletonList(
                CodeBlock.builder()
                        .add("$T thing = ", ParameterizedTypeName.get(ClassName.get(Publisher.class), getReturnTypeName(details)))
                        .add("$L.$L", member, getMethod(details)).add(generateQueryCode(details.getRequestName(), details))
                        .build()
        );
    }
}