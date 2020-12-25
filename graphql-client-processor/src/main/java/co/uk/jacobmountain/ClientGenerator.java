package co.uk.jacobmountain;

import co.uk.jacobmountain.modules.AbstractModule;
import co.uk.jacobmountain.modules.ArgumentAssemblerModule;
import co.uk.jacobmountain.modules.BlockingQueryModule;
import co.uk.jacobmountain.modules.OptionalReturnModule;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.MethodDetailsVisitor;
import com.squareup.javapoet.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ClientGenerator {

    private final Filer filer;

    private final TypeMapper typeMapper;

    private final String packageName;

    private final String dtoPackageName;

    private final Schema schema;

    private final List<AbstractModule> modules;

    public ClientGenerator(Filer filer, int maxDepth, TypeMapper typeMapper, String packageName, Schema schema) {
        this.filer = filer;
        this.typeMapper = typeMapper;
        this.packageName = packageName;
        this.dtoPackageName = packageName + ".dto";
        this.schema = schema;
        this.modules = Arrays.asList(
                new ArgumentAssemblerModule(dtoPackageName),
                new BlockingQueryModule(schema, dtoPackageName, maxDepth, typeMapper),
                new OptionalReturnModule(schema, typeMapper)
        );
    }

    public static String generateArgumentClassname(MethodDetails details) {
        String name = details.getRequestName();
        if (StringUtils.isEmpty(name)) {
            name = details.getField();
        }
        return StringUtils.capitalize(name) + "Arguments";
    }


    private void generateConstructor(TypeSpec.Builder type, List<AbstractModule.MemberVariable> variables) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        variables.forEach(var -> constructor.addParameter(var.getType(), var.getName())
                .addStatement("this.$L = $L", var.getName(), var.getName()));
        type.addMethod(constructor.build());
    }

    private MethodSpec generateImpl(Element method) {
        MethodDetails details = method.accept(new MethodDetailsVisitor(schema), typeMapper);
        log.info("{}", details);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .returns(details.getReturnType())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(details.getParameterSpec());

        modules.stream()
                .filter(it -> it.handlesAssembly(details))
                .flatMap(module -> module.assemble(details).stream())
                .forEach(builder::addStatement);

        return builder.build();
    }

    @SneakyThrows
    public void generate(TypeElement element, String suffix) {
        if (StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Invalid suffix for implementation of client: " + element.getSimpleName());
        }
        TypeSpec.Builder builder = TypeSpec.classBuilder(element.getSimpleName() + suffix)
                .addSuperinterface(ClassName.get(element))
                .addModifiers(Modifier.PUBLIC);

        this.modules.stream()
                .flatMap(it -> it.getTypeArguments().stream())
                .map(TypeVariableName::get)
                .forEach(builder::addTypeVariable);

        List<AbstractModule.MemberVariable> memberVariables = this.modules.stream()
                .map(AbstractModule::getMemberVariables)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        memberVariables.forEach(memberVariable -> builder.addField(memberVariable.getType(), memberVariable.getName(), Modifier.PRIVATE, Modifier.FINAL));

        generateConstructor(builder, memberVariables);

        element.getEnclosedElements()
                .stream()
                .peek(it -> log.info(""))
                .map(this::generateImpl)
                .forEach(builder::addMethod);

        writeToFile(builder.build());
    }


    private void writeToFile(TypeSpec spec) throws Exception {
        JavaFile.builder(packageName, spec)
                .indent("\t")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

}

