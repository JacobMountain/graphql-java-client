package com.jacobmountain.graphql.client;

import graphql.language.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DTOGenerator {

    private final FileWriter filer;

    private final TypeMapper typeMapper;

    private final Map<String, PojoBuilder> types = new HashMap<>();

    private final List<Implements> toImplement = new ArrayList<>();

    private final String packageName;

    public DTOGenerator(String packageName, FileWriter filer, TypeMapper typeMapper) {
        this.packageName = packageName;
        this.filer = filer;
        this.typeMapper = typeMapper;
    }

    /**
     * Generates the types according to the GraphQL schema
     *
     * @param types the type definitions from the GraphQL schema
     */
    public void generate(Collection<TypeDefinition> types) {
        types.forEach(this::generateDTO);
        toImplement.forEach(impl -> {
            this.types.get(impl.subtype).implement(impl.superType);
            this.types.get(impl.superType).withSubType(impl.subtype);
        });
        this.types.values().forEach(filer::write);
    }

    private void generateDTO(TypeDefinition<?> td) {
        PojoBuilder pojo = builder(td);
        if (pojo == null) {
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
        types.put(td.getName(), pojo);
        pojo.finalise();
    }

    private PojoBuilder builder(TypeDefinition<?> td) {
        if (td instanceof InterfaceTypeDefinition) {
            return PojoBuilder.newInterface(td.getName(), packageName);
        } else if (td instanceof ObjectTypeDefinition) {
            PojoBuilder builder = PojoBuilder.newType(td.getName(), packageName);
            ObjectTypeDefinition otd = ((ObjectTypeDefinition) td);
            otd.getImplements().forEach(supertype -> {
                toImplement.add(new Implements(((NamedNode<?>) supertype).getName(), td.getName()));
            });
            return builder;
        } else if (td instanceof InputObjectTypeDefinition) {
            return PojoBuilder.newInput(td.getName(), packageName);
        } else if (td instanceof EnumTypeDefinition) {
            return PojoBuilder.newEnum(td.getName(), packageName);
        } else if (td instanceof UnionTypeDefinition) {
            UnionTypeDefinition utd = (UnionTypeDefinition) td;
            PojoBuilder builder = PojoBuilder.newUnion(td.getName(), packageName);
            utd.getMemberTypes().forEach(supertype -> {
                // POJO implements interface
                String member = ((graphql.language.TypeName) supertype).getName();
                // interface has POJO as subtype
                toImplement.add(new Implements(td.getName(), member));
            });
            return builder;
        }
        log.info("{}", td);
        log.info("Unexpected type definition {}", td.getClass());
        return null;
    }

    @Data
    @AllArgsConstructor
    static class Implements {

        private String superType;

        private String subtype;

    }

}

