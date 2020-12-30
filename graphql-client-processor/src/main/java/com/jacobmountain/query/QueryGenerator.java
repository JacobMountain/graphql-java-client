package com.jacobmountain.query;

import com.jacobmountain.exceptions.FieldNotFoundException;
import com.jacobmountain.utils.Schema;
import com.jacobmountain.utils.StringUtils;
import graphql.language.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class QueryGenerator {

    private final Schema schema;

    private final int maxDepth;

    public QueryGenerator(Schema registry, int maxDepth) {
        this.schema = registry;
        this.maxDepth = maxDepth;
    }

    public String generateQuery(String request, String field, Set<String> params) {
        return doGenerateQuery(request, field, "query", params);
    }

    public String generateMutation(String request, String field, Set<String> params) {
        return doGenerateQuery(request, field, "mutation", params);
    }

    public String generateSubscription(String request, String field, Set<String> params) {
        return doGenerateQuery(request, field, "subscription", params);
    }

    private String doGenerateQuery(String request, String field, String type, Set<String> params) {
        FieldDefinition definition = schema.findField(field).orElseThrow(FieldNotFoundException.create(field));

        Set<String> args = new HashSet<>();

        String inner = generateQueryRec(field, definition, params, new HashSet<>(), 1, args).orElseThrow(RuntimeException::new);

        String collect = String.join(", ", args);

        if (!args.isEmpty()) {
            collect = "(" + collect + ")";
        }

        return generateQueryName(request, type, field) + collect + " { " + inner + " } ";
    }

    private String generateQueryName(String request, String type, String field) {
        if (StringUtils.isEmpty(request)) {
            request = StringUtils.capitalize(field);
        }
        return type + " " + request;
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

    Optional<String> generateQueryRec(String alias,
                                      FieldDefinition field,
                                      Set<String> params,
                                      Set<String> previouslyVisited,
                                      int depth,
                                      Set<String> argumentCollector) {
        String type = unwrap(field.getType());
        TypeDefinition<?> typeDefinition = schema.getTypeDefinition(type).orElse(null);

        Optional<String> optionalArgs = generateFieldArgs(field, params, argumentCollector);
        // if no args were collected, but the field has args
        if (!optionalArgs.isPresent() && !methodArgsContainAllNonNullArgs(field, params)) {
            return optionalArgs;
        }
        String args = optionalArgs.orElse("");

        // if there's no children just return that field
        if (Objects.isNull(typeDefinition) || typeDefinition.getChildren().isEmpty()) {
            return Optional.of(alias + args);
        }
        if (typeDefinition instanceof EnumTypeDefinition) {
            return Optional.of(alias);
        }
        // if the depth is too high, don't go deeper
        if (depth >= maxDepth) {
            return Optional.empty();
        }
        Set<String> visited = new HashSet<>();

        List<String> children = Stream.of(
                getChildren(typeDefinition)
                        .peek(it -> visited.add(it.getName())) // add to the list of discovered fields
                        .filter(it -> previouslyVisited.add(it.getName())) // don't add to the list if we've already discovered these fields (used with interfaces)
                        .map(definition -> generateQueryRec(definition.getName(), definition, params, new HashSet<>(), depth + 1, argumentCollector))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                schema.getTypesImplementing(typeDefinition)
                        .map(interfac -> generateQueryRec(interfac, new FieldDefinition(interfac, new TypeName(interfac)), params, visited, depth, argumentCollector))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(query -> "... on " + query)
        )
                .flatMap(Function.identity())
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

    /**
     * whether the client method contains all arguments defined in the graphql schema
     *
     * @param field  the graphql field definition
     * @param params the clients method parameters
     * @return true if the client method contains all arguments defined in the graphql schema
     */
    private boolean methodArgsContainAllNonNullArgs(FieldDefinition field, Collection<String> params) {
        return field.getInputValueDefinitions()
                .stream()
                .filter(input -> input.getType() instanceof NonNullType)
                .allMatch(nonNull -> params.contains(nonNull.getName()));
    }

    private Stream<FieldDefinition> getChildren(TypeDefinition<?> typeDefinition) {
        return typeDefinition.getChildren()
                .stream()
                .map(it -> {
                    String name = ((NamedNode<?>) it).getName();
                    Optional<FieldDefinition> childDefinition;
                    if (typeDefinition instanceof ObjectTypeDefinition) {
                        childDefinition = schema.findField((ObjectTypeDefinition) typeDefinition, name);
                    } else if (typeDefinition instanceof InterfaceTypeDefinition) {
                        childDefinition = schema.findField((InterfaceTypeDefinition) typeDefinition, name);
                    } else {
                        childDefinition = Optional.empty();
                    }
                    return childDefinition;
                })
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<String> generateFieldArgs(FieldDefinition field, Set<String> params, Set<String> argsCollector) {
        List<InputValueDefinition> args = field.getInputValueDefinitions();
        Set<String> finalParams = new HashSet<>(params);
        String collect = args.stream()
                .filter(o -> finalParams.remove(o.getName()))
                .peek(arg -> {
                    boolean nonNull = arg.getType() instanceof NonNullType;
                    String type = unwrap(arg.getType());
                    argsCollector.add(
                            "$" + arg.getName() + ": " + type + (nonNull ? "!" : "")
                    );
                })
                .map(arg -> arg.getName() + ": $" + arg.getName())
                .collect(Collectors.joining(", "));
        if (StringUtils.isEmpty(collect)) {
            return Optional.empty();
        }
        return Optional.of("(" + collect + ")");
    }

}
