package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.exceptions.FieldNotFoundException;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import graphql.language.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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

    private final List<FieldFilter> filters = Arrays.asList(
            new MaxDepthFieldFilter(),
            new AllNonNullArgs()
    );

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

    private String doGenerateQuery(String request, String field, String type, Set<String> params) {
        FieldDefinition definition = schema.findField(field).orElseThrow(FieldNotFoundException.create(field));

        Set<String> args = new HashSet<>();

        String inner = generateQueryRec(field, new QueryContext(1, definition, params, new HashSet<>()), args).orElseThrow(RuntimeException::new);

        String collect = String.join(", ", args);

        if (!args.isEmpty()) {
            collect = "(" + collect + ")";
        }

        return generateQueryName(request, type, field) + collect + " { " + inner + " } ";
    }

    Optional<String> generateQueryRec(String alias,
                                      QueryContext context,
                                      Set<String> argumentCollector) {
        String type = unwrap(context.getFieldDefinition().getType());
        TypeDefinition<?> typeDefinition = schema.getTypeDefinition(type).orElse(null);

        if (!filters.stream().allMatch(fi -> fi.shouldAddField(context))) {
            return Optional.empty();
        }

        String args = generateFieldArgs(context.getFieldDefinition(), context.getParams(), argumentCollector);

        if (Objects.isNull(typeDefinition) || typeDefinition.getChildren().isEmpty() || typeDefinition instanceof EnumTypeDefinition) {
            return Optional.of(alias + args);
        }

        Set<String> visited = new HashSet<>();
        List<String> children = Stream.of(
                getChildren(typeDefinition)
                        .peek(it -> visited.add(it.getName())) // add to the list of discovered fields
                        .filter(it -> context.getVisited().add(it.getName())) // don't add to the list if we've already discovered these fields (used with interfaces)
                        .map(definition -> generateQueryRec(definition.getName(), context.withType(definition).increment(), argumentCollector))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                schema.getTypesImplementing(typeDefinition)
                        .map(interfac -> generateQueryRec(interfac, context.withType(new FieldDefinition(interfac, new TypeName(interfac))).withVisited(visited), argumentCollector))
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

    private String generateFieldArgs(FieldDefinition field, Set<String> params, Set<String> argsCollector) {
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
            return "";
        }
        return "(" + collect + ")";
    }

    @FunctionalInterface
    interface FieldFilter {

        boolean shouldAddField(QueryContext context);

    }

    @Data
    @Builder
    @AllArgsConstructor
    static class QueryContext {

        int depth;

        FieldDefinition fieldDefinition;

        Set<String> params;

        Set<String> visited;

        QueryContext increment() {
            return new QueryContext(depth + 1, fieldDefinition, params, new HashSet<>());
        }

        QueryContext withType(FieldDefinition fieldDefinition) {
            return new QueryContext(depth, fieldDefinition, params, visited);
        }

        QueryContext withVisited(Set<String> visited) {
            return new QueryContext(depth, fieldDefinition, params, visited);
        }

    }

    static class AllNonNullArgs implements FieldFilter {
        @Override
        public boolean shouldAddField(QueryContext context) {
            return context.getFieldDefinition()
                    .getInputValueDefinitions()
                    .stream()
                    .filter(input -> input.getType() instanceof NonNullType)
                    .allMatch(nonNull -> context.getParams().contains(nonNull.getName()));
        }
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

    class MaxDepthFieldFilter implements FieldFilter {
        @Override
        public boolean shouldAddField(QueryContext context) {
            return context.getDepth() <= maxDepth;
        }
    }

}
