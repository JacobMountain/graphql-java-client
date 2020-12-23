package co.uk.jacobmountain;

import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
import graphql.language.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class QueryGenerator {

    private final Schema schema;

    private final int maxDepth;

    public QueryGenerator(TypeDefinitionRegistry registry, int maxDepth) {
        this.schema = new Schema(registry);
        this.maxDepth = maxDepth;
    }

    public String generateQuery(String field, boolean mutates) {
        FieldDefinition definition = schema.findField(field).orElse(null);

        List<String> args = new ArrayList<>();

        String inner = generateQueryRec(field, definition, 1, args).orElseThrow(RuntimeException::new);

        String collect = String.join(", ", args);

        if (!args.isEmpty()) {
            collect = "(" + collect + ")";
        }

        return generateQueryName(field, mutates) + collect + " { " + inner + " }";
    }

    private String generateQueryName(String field, boolean mutates) {
        return (mutates ? "mutation" : "query") + " " + StringUtils.capitalize(field);
    }



    private String unwrap(Type<?> type) {
        if (type instanceof ListType) {
            return unwrap(((ListType) type).getType());
        } else if (type instanceof NonNullType) {
            return unwrap(((NonNullType) type).getType());
        } else {
            return ((graphql.language.TypeName) type).getName();
        }
    }

    private Optional<String> generateQueryRec(String alias, FieldDefinition field, int depth, List<String> argumentCollector) {
        String type = unwrap(field.getType());
        TypeDefinition<?> typeDefinition = schema.getTypeDefinition(type).orElse(null);

        String args = generateFieldArgs(field, argumentCollector);

        // if there's no children just return that field
        if (Objects.isNull(typeDefinition) || typeDefinition.getChildren().isEmpty()) {
            return Optional.of(alias + args);
        }
        // if the depth is too high, don't go deeper
        if (depth >= maxDepth) {
            return Optional.empty();
        }

        List<String> children = Stream.concat(
                getChildren(typeDefinition)
                        .map(definition -> generateQueryRec(definition.getName(), definition, depth + 1, argumentCollector))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                getInterfaceChildren(typeDefinition)
                        .map(definition -> generateQueryRec(definition.getName(), definition, depth, argumentCollector))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(query -> "... on " + query)
        )
                .collect(Collectors.toList());

        if (children.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                alias + args + " { " +
                            String.join(" ", children) +
                            " __typename" +
                        " }"
        );
    }

    private Stream<FieldDefinition> getInterfaceChildren(TypeDefinition<?> typeDefinition) {
        if (!(typeDefinition instanceof InterfaceTypeDefinition)) {
            return Stream.of();
        }
        return schema.types()
                .values()
                .stream()
                .filter(it -> it instanceof ObjectTypeDefinition)
                .map(it -> (ObjectTypeDefinition) it)
                .filter(it -> it.getImplements().stream().anyMatch(ty -> ((TypeName) ty).getName().equals(typeDefinition.getName())))
                .map((ObjectTypeDefinition impl) -> {
                    String name = ((NamedNode) impl).getName();
                    return new FieldDefinition(name, new TypeName(name));
                });
    }

    private Stream<FieldDefinition> getChildren(TypeDefinition<?> typeDefinition) {
        return typeDefinition.getChildren()
                .stream()
                .map(it -> {
                    String name = ((NamedNode<?>) it).getName();
                    Optional<FieldDefinition> childDefinition;
                    if (typeDefinition instanceof ObjectTypeDefinition) {
                        childDefinition = schema.findField((ObjectTypeDefinition) typeDefinition, name);
                    } else {
                        childDefinition = schema.findField((InterfaceTypeDefinition) typeDefinition, name);
                    }
                    return childDefinition;
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private String generateFieldArgs(FieldDefinition field, List<String> argsCollector) {
        List<InputValueDefinition> args = field.getInputValueDefinitions();
        if (args.isEmpty()) {
            return "";
        }
        String collect = args.stream()
                .peek(arg -> {
                    boolean nonNull = arg.getType() instanceof NonNullType;
                    String type = unwrap(arg.getType());
                    argsCollector.add(
                            "$" + arg.getName() + ": " + type + (nonNull ? "!" : "")
                    );
                })
                .map(arg -> arg.getName() + ": $" + arg.getName())
                .collect(Collectors.joining(", "));
        return "(" + collect + ")";
    }

}
