package com.jacobmountain.graphql.client.utils;

import com.jacobmountain.graphql.client.exceptions.QueryTypeNotFoundException;
import graphql.language.*;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class Schema {

    @Delegate
    private final TypeDefinitionRegistry registry;

    @Getter
    private final ObjectTypeDefinition query;

    @Getter
    private final ObjectTypeDefinition mutation;

    @Getter
    private final ObjectTypeDefinition subscription;

    public Schema(File file) {
        this(new SchemaParser().parse(file));
    }

    public Schema(String gql) {
        this(new SchemaParser().parse(gql));
    }

    private Schema(TypeDefinitionRegistry registry) {
        this.registry = registry;
        this.query = getSchemaDefinition("query").orElseThrow(QueryTypeNotFoundException::new);
        this.mutation = getSchemaDefinition("mutation").orElse(null);
        this.subscription = getSchemaDefinition("subscription").orElse(null);
    }

    private Optional<ObjectTypeDefinition> getSchemaDefinition(String name) {
        return registry.schemaDefinition()
                .get()
                .getOperationTypeDefinitions()
                .stream()
                .filter(it -> name.equals(it.getName()))
                .findFirst()
                .flatMap(it -> getTypeDefinition(it.getTypeName().getName()))
                .map(it -> (ObjectTypeDefinition) it);
    }

    public Optional<TypeDefinition> getTypeDefinition(String name) {
        return registry.getType(name);
    }

    public Optional<FieldDefinition> findField(String field) {
        return OptionalUtils.first(
                findField(query, field),
                () -> findField(mutation, field),
                () -> findField(subscription, field)
        );
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

    public String getQueryTypeName() {
        return query.getName();
    }

    public Optional<String> getMutationTypeName() {
        return Optional.ofNullable(mutation).map(ObjectTypeDefinition::getName);
    }

    public Optional<String> getSubscriptionTypeName() {
        return Optional.ofNullable(subscription).map(ObjectTypeDefinition::getName);
    }

    /**
     * Takes a type definition and returns a stream of types that implement it
     *
     * @param typeDefinition the possible InterfaceTypeDefinition
     * @return
     */
    public Stream<String> getTypesImplementing(TypeDefinition<?> typeDefinition) {
        if (!(typeDefinition instanceof InterfaceTypeDefinition)) {
            return Stream.empty();
        }
        return types()
                .values()
                .stream()
                .filter(it -> it instanceof ObjectTypeDefinition)
                .map(it -> (ObjectTypeDefinition) it)
                .filter(it -> it.getImplements().stream().anyMatch(ty -> ((TypeName) ty).getName().equals(typeDefinition.getName())))
                .map((ObjectTypeDefinition impl) -> ((NamedNode<?>) impl).getName());
    }

    public Stream<FieldDefinition> getChildren(TypeDefinition<?> typeDefinition) {
        return typeDefinition.getChildren()
                .stream()
                .map(it -> {
                    String name = ((NamedNode<?>) it).getName();
                    Optional<FieldDefinition> childDefinition;
                    if (typeDefinition instanceof ObjectTypeDefinition) {
                        childDefinition = findField((ObjectTypeDefinition) typeDefinition, name);
                    } else if (typeDefinition instanceof InterfaceTypeDefinition) {
                        childDefinition = findField((InterfaceTypeDefinition) typeDefinition, name);
                    } else {
                        childDefinition = Optional.empty();
                    }
                    return childDefinition;
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

}
