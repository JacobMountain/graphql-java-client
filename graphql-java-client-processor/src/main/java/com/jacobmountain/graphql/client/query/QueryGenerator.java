package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.exceptions.FieldNotFoundException;
import com.jacobmountain.graphql.client.query.filters.AllNonNullArgsFieldFilter;
import com.jacobmountain.graphql.client.query.filters.MaxDepthFieldFilter;
import com.jacobmountain.graphql.client.query.filters.SelectionFieldFilter;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection;
import graphql.com.google.common.collect.Streams;
import graphql.language.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class QueryGenerator {

    private final Schema schema;

    public QueryGenerator(Schema registry) {
        this.schema = registry;
    }

    public QueryBuilder query() {
        return new QueryBuilder("query");
    }

    public QueryBuilder mutation() {
        return new QueryBuilder("mutation");
    }

    public QueryBuilder subscription() {
        return new QueryBuilder("subscription");
    }

    private String doGenerateQuery(String request, String field, String type, Set<String> params, List<FieldFilter> filters) {
        FieldDefinition definition = schema.findField(field).orElseThrow(FieldNotFoundException.create(field));

        Set<String> args = new HashSet<>();

        String inner = generateQueryRec(field, new QueryContext(0, definition, params, new HashSet<>()), args, filters).orElseThrow(RuntimeException::new);

        String collect = String.join(", ", args);

        if (!args.isEmpty()) {
            collect = "(" + collect + ")";
        }

        return generateQueryName(request, type, field) + collect + " { " + inner + " } ";
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

    private String generateQueryName(String request, String type, String field) {
        if (StringUtils.isEmpty(request)) {
            request = StringUtils.capitalize(field);
        }
        return type + " " + request;
    }

    private Optional<String> generateQueryRec(String alias,
                                              QueryContext context,
                                              Set<String> argumentCollector,
                                              List<FieldFilter> filters) {
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
        List<String> children = Streams.concat(
                schema.getChildren(typeDefinition)
                        .peek(it -> visited.add(it.getName())) // add to the list of discovered fields
                        .filter(it -> context.getVisited().add(it.getName())) // don't add to the list if we've already discovered these fields (used with interfaces)
                        .map(definition -> generateQueryRec(
                                definition.getName(),
                                context.withType(definition).increment(),
                                argumentCollector,
                                filters
                        ))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                schema.getTypesImplementing(typeDefinition)
                        .map(interfac -> generateQueryRec(
                                interfac,
                                context.withType(new FieldDefinition(interfac, new TypeName(interfac))).withVisited(visited),
                                argumentCollector,
                                filters
                        ))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(query -> "... on " + query)
        ).collect(Collectors.toList());

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

    public class QueryBuilder {

        private final String type;

        private final List<FieldFilter> filters = new ArrayList<>();

        QueryBuilder(String type) {
            this.type = type;
        }

        public QueryBuilder maxDepth(int maxDepth) {
            this.filters.add(new MaxDepthFieldFilter(maxDepth));
            return this;
        }

        public QueryBuilder select(List<GraphQLFieldSelection> selections) {
            this.filters.add(new SelectionFieldFilter(selections));
            return this;
        }

        public String build(String request, String field, Set<String> params) {
            this.filters.add(new AllNonNullArgsFieldFilter());
            return doGenerateQuery(request, field, type, params, filters);
        }

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

}
