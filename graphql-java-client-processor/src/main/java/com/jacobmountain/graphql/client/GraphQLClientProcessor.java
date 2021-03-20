package com.jacobmountain.graphql.client;

import com.google.auto.service.AutoService;
import com.jacobmountain.graphql.client.annotations.GraphQLClient;
import com.jacobmountain.graphql.client.code.ClientGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Slf4j
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.jacobmountain.graphql.client.*")
public class GraphQLClientProcessor extends AbstractProcessor {

    private Filer filer;

    private ClientGenerator clientGenerator;

    private Messager messager;

    private Path root;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.clientGenerator = new ClientGenerator(this.filer);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GraphQLClient.class);
        for (Element el : elements) {
            if (!(el.getKind() == ElementKind.CLASS || el.getKind() == ElementKind.INTERFACE)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can't be applied to class");
                return true;
            }
        }
        final List<Input> interfaces = elements.stream()
                .map(el -> (TypeElement) el)
                .map(type -> new Input(type, getRoot(), processingEnv.getElementUtils().getPackageOf(type).toString()))
                .collect(toList());
        interfaces.stream()
                .filter(new OncePerSchemaPredicate())
                .forEach(this::generateJavaDataClasses);
        return interfaces.stream()
                .peek(this::generateClientImplementation)
                .count() > 0;
    }

    private static class OncePerSchemaPredicate implements Predicate<Input> {
        private final Set<String> schemas = new HashSet<>();

        @Override
        public boolean test(Input input) {
            return schemas.add(input.getAnnotation().schema());
        }
    }

    private void generateJavaDataClasses(Input input) {
        log.info("Generating java classes from GraphQL schema");
        DTOGenerator dtoGenerator = new DTOGenerator(input.getDtoPackage(), new FileWriter(this.filer), input.getTypeMapper());
        dtoGenerator.generate(input.getSchema().types().values());
    }

    @SneakyThrows
    private void generateClientImplementation(Input client) {
        log.info("Generating java implementation of {}", client.getElement().getSimpleName());
        clientGenerator.generate(client);
    }

    @SneakyThrows
    private Path getRoot() {
        if (root == null) {
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null);
            root = Paths.get(resource.toUri())
                    .getParent() // main
                    .getParent() // java
                    .getParent() // classes
                    .getParent() // build
                    .getParent();// project
            resource.delete();
        }
        return root;
    }

}
