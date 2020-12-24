package co.uk.jacobmountain;

import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.MethodDetailsVisitor;
import co.uk.jacobmountain.visitor.Parameter;
import com.squareup.javapoet.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

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

    public static String generateArgumentClassname(MethodDetails details) {
        String name = details.getName();
        if (StringUtils.isEmpty(name)) {
            name = details.getField();
        }
        return StringUtils.capitalize(name) + "Arguments";
    }

    private ParameterizedTypeName generateTypeName(Schema schema) {
        ClassName fetcher = ClassName.get(Fetcher.class);
        ClassName query = ClassName.get(this.dtoPackageName, schema.getQueryTypeName());
        if (schema.getMutationTypeName().isPresent())
            return ParameterizedTypeName.get(
                    fetcher,
                    query,
                    ClassName.get(this.dtoPackageName, schema.getMutationTypeName().get()),
                    TypeVariableName.get("Error")
            );
        return ParameterizedTypeName.get(
                fetcher,
                query,
                ClassName.get(Void.class),
                TypeVariableName.get("Error")
        );
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

    private MethodSpec generateImpl(Element method, Schema schema) {
        MethodDetails details = method.accept(new MethodDetailsVisitor(schema), typeMapper);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .returns(details.getReturnType())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(details.getParameterSpec());
        assembleArguments(details).forEach(builder::addStatement);
        assembleFetchAndReturn(details, schema).forEach(builder::addStatement);
        return builder.build();
    }

    @SneakyThrows
    public void generate(Schema schema, TypeElement element) {
        ParameterizedTypeName fetcherType = generateTypeName(schema);
        TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName() + "Graph")
                .addSuperinterface(ClassName.get(element))
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("Error"))
                .addField(fetcherType, "fetcher", Modifier.PRIVATE, Modifier.FINAL);

        generateConstructor(builder, fetcherType);

        element.getEnclosedElements()
                .stream()
                .map(method -> generateImpl(method, schema))
                .forEach(builder::addMethod);

        writeToFile(builder.build());
    }

    private List<CodeBlock> assembleFetchAndReturn(MethodDetails details, Schema schema) {
        boolean wrapInOptional = details.getReturnType() instanceof ParameterizedTypeName &&
                ((ParameterizedTypeName) details.getReturnType()).rawType.equals(ClassName.get(Optional.class));
        CodeBlock.Builder builder = CodeBlock.builder();
        if (wrapInOptional) {
            builder.add("return $T.ofNullable(\n", Optional.class)
                    .indent();
        } else {
            builder.add("return ");
        }
        builder.add("fetcher")
                .add(generateQuery(details.getName(), schema, details))
                .add("\n").indent()
                .add(".getData()")
                .add("\n")
                .add(".$L()", StringUtils.camelCase("get", details.getField()));
        if (wrapInOptional) {
            builder.add("\n)").unindent();
        }
        builder.unindent();
        return Collections.singletonList(builder.build());
    }

    private CodeBlock generateQuery(String request, Schema schema, MethodDetails details) {
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

    private List<CodeBlock> assembleArguments(MethodDetails details) {
        List<Parameter> parameters = details.getParameters();
        if (parameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<CodeBlock> ret = new ArrayList<>();
        TypeName type = ClassName.get(dtoPackageName, generateArgumentClassname(details));
        ret.add(CodeBlock.of("$T args = new $T()", type, type));
        details.getParameters()
                .stream()
                .map(this::setArgumentField)
                .forEach(ret::add);
        return ret;
    }

    private CodeBlock setArgumentField(Parameter param) {
        String parameter = param.getName();
        String field = param.getField();
        CodeBlock value = CodeBlock.of("$L", parameter);
        if (!param.isNullable()) {
            value = CodeBlock.of("$T.requireNonNull($L, $S)", Objects.class, parameter, String.format("%s is not nullable", parameter));
        }
        return CodeBlock.of("args.set$L($L)", StringUtils.capitalize(field), value);
    }

    private void writeToFile(TypeSpec spec) throws Exception {
        JavaFile.builder(packageName, spec)
                .indent("\t")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

}

