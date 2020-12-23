package co.uk.jacobmountain.utils;

import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.Optional;

public class Schema {

    @Delegate
    private final TypeDefinitionRegistry registry;

    private final ObjectTypeDefinition query;

    private final ObjectTypeDefinition mutation;

    public Schema(TypeDefinitionRegistry registry) {
        this.registry = registry;
        this.query = (ObjectTypeDefinition) getTypeDefinition("Query").orElseThrow(RuntimeException::new);
        this.mutation = (ObjectTypeDefinition) getTypeDefinition("Mutation").orElse(null);
    }

    public Optional<TypeDefinition> getTypeDefinition(String name) {
        return registry.getType(name);
    }

    public Optional<FieldDefinition> findField(String field) {
        return Optional.of(findField(query, field))
                .orElseGet(() -> findField(mutation, field));
    }

    public Optional<FieldDefinition> findField(ObjectTypeDefinition parent, String field) {
        return Optional.ofNullable(parent)
                .map(ObjectTypeDefinition::getFieldDefinitions)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(it -> it.getName().equals(field))
                .findAny();
    }

    public Optional<FieldDefinition> findField(InterfaceTypeDefinition parent, String field) {
        return parent.getFieldDefinitions()
                .stream()
                .filter(it -> it.getName().equals(field))
                .findAny();
    }
}
