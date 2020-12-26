package co.uk.jacobmountain.utils;

import co.uk.jacobmountain.exceptions.QueryTypeNotFoundException;
import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schema {

    @EqualsAndHashCode.Include
    private final File file;

    @Delegate
    private final TypeDefinitionRegistry registry;

    @Getter
    private final ObjectTypeDefinition query;

    @Getter
    private final ObjectTypeDefinition mutation;

    @Getter
    private final ObjectTypeDefinition subscription;

    public Schema(File file) {
        this(file, new SchemaParser().parse(file));
    }

    public Schema(String gql) {
        this(null, new SchemaParser().parse(gql));
    }

    private Schema(File file, TypeDefinitionRegistry registry) {
        this.file = file;
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
        return optionals(
                findField(query, field),
                () -> findField(mutation, field),
                () -> findField(subscription, field)
        );
    }

    private <T> Optional<T> optionals(Optional<T> first, Supplier<Optional<T>>... later) {
        if (first.isPresent()) {
            return first;
        }
        if (later.length == 0) {
            return Optional.empty();
        }
        Optional<T> head = later[0].get();
        return optionals(head, Arrays.copyOfRange(later, 1, later.length));
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

}
