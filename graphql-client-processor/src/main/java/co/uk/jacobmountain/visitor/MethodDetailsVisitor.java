package co.uk.jacobmountain.visitor;

import co.uk.jacobmountain.GraphQLQuery;
import co.uk.jacobmountain.TypeMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.stream.Collectors;

@Slf4j
public class MethodDetailsVisitor extends ElementKindVisitor8<MethodDetails, TypeMapper> {
    @Override
    public MethodDetails visitExecutableAsMethod(ExecutableElement e, TypeMapper typeMapper) {
        GraphQLQuery annotation = e.getAnnotation(GraphQLQuery.class);
        return MethodDetails.builder()
                .returnType(typeMapper.defaultPackage(TypeName.get(e.getReturnType())))
                .field(annotation.value())
                .mutation(annotation.mutation())
                .parameters(
                        e.getParameters()
                                .stream()
                                .map(parameter -> ParameterSpec.builder(
                                        typeMapper.defaultPackage(ClassName.get(parameter.asType())),
                                        parameter.getSimpleName().toString()
                                        ).build()
                                )
                                .collect(Collectors.toList())
                )
                .build();
    }
}
