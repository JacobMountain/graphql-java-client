package co.uk.jacobmountain;

import co.uk.jacobmountain.visitor.MethodDetails;
import co.uk.jacobmountain.visitor.MethodDetailsVisitor;
import graphql.language.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

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
                it.build().writeTo(filer);
            } catch (Exception e) {
                log.error("Failed to create class", e);
            }
        });
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public void generateArgumentDTOs(TypeElement client) {
        client.getEnclosedElements()
                .stream()
                .map(method -> method.accept(new MethodDetailsVisitor(null), typeMapper))
                .filter(MethodDetails::hasParameters) // don't generate argument classes for methods without args
                .map(details -> {
                    String name = ClientGenerator.generateArgumentClassname(details);
                    PojoBuilder builder = PojoBuilder.newClass(name, packageName);
                    details.getParameters()
                            .forEach(variable -> {
                                String field = variable.getName();
                                if (variable.getAnnotation() != null) {
                                    field = variable.getAnnotation().value();
                                }
                                builder.withField(variable.getType(), field);
                            });
                    return new AbstractMap.SimpleEntry<>(name, builder);
                })
                .filter(distinctByKey(AbstractMap.SimpleEntry::getKey)) // don't rebuild new classes if two requests share args
                .forEach(entry -> {
                    try {
                        entry.getValue().build().writeTo(filer);
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
        } else if (td instanceof EnumTypeDefinition) {
            EnumTypeDefinition enumTypeDefinition = (EnumTypeDefinition) td;
            enumTypeDefinition.getEnumValueDefinitions()
                    .forEach(pojo::withEnumValue);
        }
        log.info("}");
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
        } else if (td instanceof EnumTypeDefinition) {
            return PojoBuilder.newEnum(td.getName(), packageName);
        } else if (td instanceof UnionTypeDefinition) {
            UnionTypeDefinition utd = (UnionTypeDefinition) td;
            PojoBuilder builder = PojoBuilder.newInterface(td.getName(), packageName);
            utd.getMemberTypes().forEach(supertype -> {
                // POJO implements interface
                String member = ((graphql.language.TypeName) supertype).getName();
                builder.withSubType(member);
                // interface has POJO as subtype
                // TODO interface has to be defined before impl, improve?
                types.get(member).implement(td.getName());
            });
            return builder;
        }
        log.info("Unexpected type definition {}", td.getClass());
        return null;
    }
}

