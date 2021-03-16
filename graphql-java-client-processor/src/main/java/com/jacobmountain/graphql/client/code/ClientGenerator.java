package com.jacobmountain.graphql.client.code;

import com.jacobmountain.graphql.client.PojoBuilder;
import com.jacobmountain.graphql.client.TypeMapper;
import com.jacobmountain.graphql.client.code.blocking.BlockingAssembler;
import com.jacobmountain.graphql.client.code.reactive.ReactiveAssembler;
import com.jacobmountain.graphql.client.query.QueryGenerator;
import com.jacobmountain.graphql.client.utils.AnnotationUtils;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.ClientDetails;
import com.jacobmountain.graphql.client.visitor.ClientDetailsVisitor;
import com.jacobmountain.graphql.client.visitor.ClientDetailsVisitorArgs;
import com.jacobmountain.graphql.client.visitor.MethodDetails;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * ClientGenerator generates the implementation of the interface annotated with @GraphQLClient
 */
@Slf4j
@RequiredArgsConstructor
public class ClientGenerator {

    private final Filer filer;

    private final TypeMapper typeMapper;

    private final String packageName;

    private final Schema schema;

    private final Assembler assembler;

    public ClientGenerator(Filer filer, TypeMapper typeMapper, String packageName, Schema schema, boolean reactive) {
        this.filer = filer;
        this.typeMapper = typeMapper;
        this.packageName = packageName;
        this.schema = schema;
        QueryGenerator queryGenerator = new QueryGenerator(schema);
        if (reactive) {
            this.assembler = new ReactiveAssembler(queryGenerator, schema, typeMapper);
        } else {
            this.assembler = new BlockingAssembler(queryGenerator, schema, typeMapper);
        }
    }

    /**
     * Generates the implementation of the @GraphQLClient interface
     *
     * @param element the Element that has the @GraphQLClient on
     * @param suffix  the implementations suffix
     */
    public void generate(Element element, String suffix) throws IOException {
        if (StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Invalid suffix for implementation of client: " + element.getSimpleName());
        }
        ClientDetails client = element.accept(
                new ClientDetailsVisitor(),
                new ClientDetailsVisitorArgs(schema, typeMapper)
        );

        // Generate the class
        TypeSpec.Builder builder = TypeSpec.classBuilder(client.getName() + suffix)
                .addSuperinterface(client.getClientInterface())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationUtils.generated());

        // Add type arguments to the client
        for (TypeVariableName typeVariableName : this.assembler.getTypeArguments()) {
            builder.addTypeVariable(typeVariableName);
        }

        // Add any necessary member variables to the client
        List<MemberVariable> memberVariables = assembler.getMemberVariables(client);
        for (MemberVariable memberVariable : memberVariables) {
            builder.addField(
                    memberVariable.getType(), memberVariable.getName(), Modifier.PRIVATE, Modifier.FINAL
            );
        }

        // generate the constructor
        builder.addMethod(generateConstructor(memberVariables));

        // for each method on the interface, generate its implementation
        for (MethodDetails method : client.getMethods()) {
            generateMethodImplementation(builder, method, client);
        }

        writeToFile(builder.build());
    }

    /**
     * Generates a constructor which takes in any required member variables (usually the fetcher)
     *
     * @param variables the required member variables
     */
    private MethodSpec generateConstructor(List<MemberVariable> variables) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        variables.forEach(var -> constructor.addParameter(var.getType(), var.getName())
                .addStatement("this.$L = $L", var.getName(), var.getName()));
        return constructor.build();
    }

    /**
     * Generates the client implementation of one method of the interface
     *
     * @param method the method of the @GraphQLClient annotated interface
     */
    private void generateMethodImplementation(TypeSpec.Builder clazz, MethodDetails method, ClientDetails client) {
        log.info("");
        log.info("{}", method);

        generateArgumentDTO(method)
                .ifPresent(clazz::addType);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getMethodName())
                .returns(method.getReturnType())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(method.getParameterSpec());

        this.assembler.assemble(client, method).forEach(builder::addStatement);

        clazz.addMethod(builder.build());
    }

    public Optional<TypeSpec> generateArgumentDTO(MethodDetails details) {
        return Optional.of(details)
                .filter(MethodDetails::hasParameters)
                .map(it -> {
                    String name = details.getArgumentClassname();
                    PojoBuilder builder = PojoBuilder.newType(name, packageName).staTic();
                    details.getParameters()
                            .forEach(variable -> {
                                String field = variable.getName();
                                if (variable.getAnnotation() != null) {
                                    field = variable.getAnnotation().value();
                                }
                                builder.withField(variable.getType(), field);
                            });
                    return builder.buildClass();
                });
    }

    private void writeToFile(TypeSpec spec) throws IOException {
        JavaFile.builder(packageName, spec)
                .indent("\t")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

}

