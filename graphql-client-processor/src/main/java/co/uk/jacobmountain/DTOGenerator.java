package co.uk.jacobmountain;

import graphql.language.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;

@Slf4j
public class DTOGenerator {

    private final Filer filer;

    private final TypeMapper typeMapper;

    private final Map<String, PojoBuilder> types = new HashMap<>();

    private final String packageName;

    public DTOGenerator(String packageName, Filer filer, TypeMapper typeMapper) {
        this.packageName = packageName;
        this.filer = filer;
        this.typeMapper =  typeMapper;
    }

    public void generate(Collection<TypeDefinition> types) {
        types.forEach(this::generateDTO);
        this.types.values().forEach(it -> {
                    try {
                        it.build(filer);
                    } catch (Exception e) {
                        log.error("Failed to create class", e);
                    }
                });
    }

    public void generateArgumentDTOs(TypeElement client) {
        client.getEnclosedElements()
                .stream()
                .map(method -> method.accept(new ClientGenerator.MethodDetailsVisitor(), typeMapper))
                .filter(details -> !details.getParameters().isEmpty()) // don't generate argument classes for methods without args
                .forEach(details -> {
                    PojoBuilder builder = PojoBuilder.newClass(ClientGenerator.generateArgumentClassname(details.getField()), packageName);
                    details.getParameters()
                            .forEach(variable -> {
                                builder.withField(variable.type, variable.name);
                            });
                    try {
                        builder.build(filer);
                    } catch (IOException e) {
                        log.error("Failed to create class", e);
                    }
                });
    }

    private void generateDTO(TypeDefinition<?> td) {
        PojoBuilder pojo = builder(td);
        if (pojo == null){
            return;
        }
        td.getChildren()
                .stream()
                .filter(it -> it instanceof NamedNode)
                .filter(it -> !((NamedNode<?>) it).getName().startsWith("_"))
                .forEach(it -> {
                    if (it instanceof FieldDefinition) {
                        FieldDefinition named = (FieldDefinition) it;
                        pojo.withField(
                                typeMapper.getType(named.getType()),
                                named.getName()
                        );
                    }
                });
        if (td instanceof InputObjectTypeDefinition) {
            InputObjectTypeDefinition inputObjectTypeDefinition = (InputObjectTypeDefinition) td;
            inputObjectTypeDefinition.getInputValueDefinitions()
                    .forEach(it -> pojo.withField(
                            typeMapper.getType(it.getType()),
                            it.getName()
                    ));
        }
        types.put(td.getName(), pojo);
    }

    private PojoBuilder builder(TypeDefinition<?> td) {
        if (td instanceof InterfaceTypeDefinition) {
            return PojoBuilder.newInterface(td.getName(), packageName);
        } else if (td instanceof ObjectTypeDefinition) {
            PojoBuilder builder = PojoBuilder.newClass(td.getName(), packageName);
            ObjectTypeDefinition otd = ((ObjectTypeDefinition) td);
            otd.getImplements().forEach(supertype -> {
                // POJO implements interface
                builder.implement(((graphql.language.TypeName) supertype).getName());
                // interface has POJO as subtype
                // TODO interface has to be defined before impl, improve?
                types.get(((NamedNode) supertype).getName()).withSubType(td.getName());
            });
            return builder;
        } else if (td instanceof InputObjectTypeDefinition) {
            return PojoBuilder.newClass(td.getName(), packageName);
        }
        log.info("Unexpected type definition {}", td.getClass());
        return null;
    }
}

