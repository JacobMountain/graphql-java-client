package co.uk.jacobmountain.visitor;

import co.uk.jacobmountain.GraphQLArgument;
import co.uk.jacobmountain.GraphQLQuery;
import co.uk.jacobmountain.TypeMapper;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MethodDetailsVisitor extends ElementKindVisitor8<MethodDetails, TypeMapper> {

    private final Schema schema;

    public MethodDetailsVisitor(TypeDefinitionRegistry registry) {
        this.schema = registry != null ? new Schema(registry) : null;
    }

    @Override
    public MethodDetails visitExecutableAsMethod(ExecutableElement e, TypeMapper typeMapper) {
        GraphQLQuery annotation = e.getAnnotation(GraphQLQuery.class);
        return MethodDetails.builder()
                .name(annotation.request())
                .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                .field(annotation.value())
                .mutation(annotation.mutation())
                .parameters(
                        e.getParameters()
                                .stream()
                                .map(parameter -> {
                                            String className = parameter.getSimpleName().toString();
                                            return Parameter.builder()
                                                    .type(typeMapper.defaultPackage(ClassName.get(parameter.asType())))
                                                    .name(className)
                                                    .annotation(parameter.getAnnotation(GraphQLArgument.class))
                                                    .nullable(
                                                            isNullableArg(annotation.value(), className)
                                                    )
                                                    .build();
                                        }
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }

    private boolean isNullableArg(String field, String arg) {
        if (schema == null) {
            return true;
        }
        Optional<FieldDefinition> type = schema.findField(field);
        if (!type.isPresent()) {
            return true;
        }
        InputValueDefinition definition = type.get()
                .getInputValueDefinitions()
                .stream()
                .filter(it -> StringUtils.equals(it.getName(), arg))
                .findFirst()
                .orElse(null);
        return definition == null || !definition.getType().getClass().equals(NonNullType.class);
    }

}
