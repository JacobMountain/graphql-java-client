package com.jacobmountain.graphql.client.code;

import com.jacobmountain.graphql.client.Input;
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
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * ClientGenerator generates the implementation of the interface annotated with @GraphQLClient
 */
@Slf4j
public class ClientGenerator {

    private final Filer filer;

    public ClientGenerator(Filer filer) {
        this.filer = filer;
    }

    /**
     * Generates the implementation of the @GraphQLClient interface
     *
     * @param input the Input data required to generate the client implementation
     */
    public void generate(Input input) throws IOException {
        if (StringUtils.isEmpty(input.getAnnotation().implSuffix())) {
            throw new IllegalArgumentException("Invalid suffix for implementation of client: " + input.getElement().getSimpleName());
        }
        final Schema schema = input.getSchema();
        final TypeMapper typeMapper = input.getTypeMapper();
        ClientDetails client = input.getElement().accept(
                new ClientDetailsVisitor(),
                new ClientDetailsVisitorArgs(schema, typeMapper)
        );

        Assembler assembler = initializeAssembler(schema, typeMapper, input.isReactive());

        // Generate the class
        TypeSpec.Builder builder = TypeSpec.classBuilder(client.getName() + input.getAnnotation().implSuffix())
                .addSuperinterface(client.getClientInterface())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationUtils.generated());

        // Add type arguments to the client
        for (TypeVariableName typeVariableName : assembler.getTypeArguments()) {
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
            generateArgumentDTO(method, input.getDtoPackage()).ifPresent(builder::addType);
            builder.addMethod(generateMethodImplementation(assembler, method, client));
        }

        writeToFile(builder.build(), input.getPackage());
    }

    private Assembler initializeAssembler(Schema schema, TypeMapper typeMapper, boolean reactive) {
        QueryGenerator queryGenerator = new QueryGenerator(schema);
        if (reactive) {
            return new ReactiveAssembler(queryGenerator, schema, typeMapper);
        } else {
            return new BlockingAssembler(queryGenerator, schema, typeMapper);
        }
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
     * @param assembler the assembler that creates the code for the method
     * @param method    the method details
     * @param client    the client classes details
     * @return the code of the method
     */
    private MethodSpec generateMethodImplementation(Assembler assembler,
                                                    MethodDetails method,
                                                    ClientDetails client) {
        log.info("");
        log.info("{}", method);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getMethodName())
                .returns(method.getReturnType())
                .addModifiers(Modifier.PUBLIC)
                .addParameters(method.getParameterSpec());

        assembler.assemble(client, method).forEach(builder::addStatement);

        return builder.build();
    }

    public Optional<TypeSpec> generateArgumentDTO(MethodDetails details, String packageName) {
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

    private void writeToFile(TypeSpec spec, String packageName) throws IOException {
        JavaFile.builder(packageName, spec)
                .indent("\t")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

}

