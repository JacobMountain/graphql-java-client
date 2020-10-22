package co.uk.jacobmountain;

import co.uk.jacobmountain.utils.StringUtils;
import com.squareup.javapoet.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.language.TypeName.newTypeName;

@Slf4j
@RequiredArgsConstructor
public class ClientGenerator {

    private final Filer filer;

    private final int maxDepth;

    private final TypeMapper typeMapper;

    private final String packageName;

    private final String dtoPackageName;

    public ClientGenerator(Filer filer, int maxDepth, TypeMapper typeMapper, String packageName) {
        this.filer = filer;
        this.maxDepth = maxDepth;
        this.typeMapper = typeMapper;
        this.packageName = packageName;
        this.dtoPackageName = packageName + ".dto";
    }

    private ParameterizedTypeName generateTypeName(TypeDefinitionRegistry schema) {
        if (schema.hasType(newTypeName("Mutation").build()))
            return ParameterizedTypeName.get(
                    ClassName.get(Fetcher.class),
                    ClassName.get(this.dtoPackageName, "Query"),
                    ClassName.get(this.dtoPackageName, "Mutation")
            );
        return ParameterizedTypeName.get(
                ClassName.get(Fetcher.class),
                ClassName.get(this.dtoPackageName, "Query"),
                ClassName.get(Void.class)
        );
    }

    @SneakyThrows
    public void generate(TypeDefinitionRegistry schema, TypeElement element) {
        ParameterizedTypeName fetcherType = generateTypeName(schema);
        TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName() + "Graph")
                .addSuperinterface(ClassName.get(element))
                .addModifiers(Modifier.PUBLIC)
                .addField(fetcherType, "fetcher", Modifier.PRIVATE, Modifier.FINAL);

        generateConstructor(builder, fetcherType);

        element.getEnclosedElements()
                .stream()
                .map(method -> generateImpl(method, schema))
                .forEach(builder::addMethod);

        writeToFile(builder.build());
    }

    private void generateConstructor(TypeSpec.Builder builder, ParameterizedTypeName fetcherType) {
        builder.addMethod(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(fetcherType, "fetcher")
                        .addStatement("this.fetcher = fetcher")
                        .build()
        );
    }

    private MethodSpec generateImpl(Element method, TypeDefinitionRegistry schema) {
        MethodDetails details = method.accept(new MethodDetailsVisitor(), typeMapper);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .returns(details.returnType)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(details.getParameters());
        List<CodeBlock> args = assembleArguments(details);
        args.forEach(builder::addStatement);
        return builder.addStatement(
                CodeBlock.builder()
                        .add("return fetcher")
                        .add(details.mutation ? generateMutationCode(schema, details.getField(), args) : generateQueryCode(schema, details.getField(), args))
                        .add(".getData()")
                        .add(".$L()", StringUtils.camelCase("get", details.getField()))
                        .build()
        )
                .build();
    }

    private CodeBlock generateQueryCode(TypeDefinitionRegistry schema, String field, List<CodeBlock> args) {
        String query = generateQuery(schema, field, maxDepth);
        if (args.isEmpty()) {
            return CodeBlock.of(".query(\"$L\")", query);
        }
        return CodeBlock.of(".query(\"$L\", args)", query);
    }

    private CodeBlock generateMutationCode(TypeDefinitionRegistry schema, String field, List<CodeBlock> args) {
        String query = generateMutation(schema, field, maxDepth);
        if (args.isEmpty()) {
            return CodeBlock.of(".mutate(\"$L\")", query);
        }
        return CodeBlock.of(".mutate(\"$L\", args)", query);
    }

    private String generateQuery(TypeDefinitionRegistry schema, String field, int depth) {
        QueryGenerator generator = new QueryGenerator(schema, depth);
        return generator.generateQuery(field, false);
    }

    private String generateMutation(TypeDefinitionRegistry schema, String field, int depth) {
        QueryGenerator generator = new QueryGenerator(schema, depth);
        return generator.generateQuery(field, true);
    }

    public static String generateArgumentClassname(String field) {
        return StringUtils.capitalize(field) + "Arguments";
    }

    private List<CodeBlock> assembleArguments(MethodDetails details) {
        List<ParameterSpec> parameters = details.getParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<CodeBlock> ret = new ArrayList<>();
        TypeName type = ClassName.get(dtoPackageName, generateArgumentClassname(details.getField()));
        ret.add(CodeBlock.of("$T args = new $T()", type, type));
        details.getParameters()
                .forEach(param -> ret.add(CodeBlock.of("args.set$L($L)", StringUtils.capitalize(param.name), param.name)));
        return ret;
    }

    private void writeToFile(TypeSpec spec) throws Exception {
        JavaFile.builder(packageName, spec)
                .indent("\t")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

    @Data
    @Builder
    public static class MethodDetails {

        private TypeName returnType;

        private String field;

        @Singular
        private List<ParameterSpec> parameters;

        private boolean mutation;

    }

    @Slf4j
    public static class MethodDetailsVisitor extends ElementKindVisitor8<MethodDetails, TypeMapper> {
        @Override
        public MethodDetails visitExecutableAsMethod(ExecutableElement e, TypeMapper typeMapper) {
            GraphQLQuery annotation = e.getAnnotation(GraphQLQuery.class);
            TypeName returnType = ClassName.get(e.getReturnType());
            return MethodDetails.builder()
                    .returnType(typeMapper.defaultPackage(returnType))
                    .field(annotation.value())
                    .mutation(annotation.mutation())
                    .parameters(
                            e.getParameters()
                                    .stream()
                                    .map(parameter -> ParameterSpec.builder(
                                            typeMapper.defaultPackage(ClassName.get(parameter.asType())),
                                            parameter.getSimpleName().toString())
                                            .build()
                                    )
                                    .collect(Collectors.toList())
                    )
                    .build();
        }
    }

}

