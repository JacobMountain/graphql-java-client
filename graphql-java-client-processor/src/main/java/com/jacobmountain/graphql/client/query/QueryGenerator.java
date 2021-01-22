package com.jacobmountain.graphql.client.query;

import com.jacobmountain.graphql.client.annotations.GraphQLField;
import com.jacobmountain.graphql.client.exceptions.FieldNotFoundException;
import com.jacobmountain.graphql.client.query.filters.AllNonNullArgsFieldFilter;
import com.jacobmountain.graphql.client.query.filters.FieldDuplicationFilter;
import com.jacobmountain.graphql.client.query.filters.MaxDepthFieldFilter;
import com.jacobmountain.graphql.client.query.filters.SelectionFieldFilter;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import graphql.com.google.common.collect.Streams;
import graphql.language.*;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.Opt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        String inner = generateFieldSelection(field, new QueryContext(null, 1, definition, params), args, filters).orElseThrow(RuntimeException::new);

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

    public Optional<String> generateFieldSelection(String alias,
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

        return selectChildren(typeDefinition, context, argumentCollector, filters)
                .map(children -> alias + args + " " + children);
    }

    private Optional<String> selectChildren(TypeDefinition<?> typeDefinition,
                                            QueryContext context,
                                            Set<String> argumentCollector,
                                            List<FieldFilter> filters) {
        List<FieldSelector> selectors = Arrays.asList(
                new DefaultFieldSelector(schema, this),
                new InlineFragmentRenderer(schema, this)
        );
        final List<String> children = selectors.stream()
                .flatMap(selector -> selector.selectFields(typeDefinition, context, argumentCollector, filters))
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                "{ " +
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

        public QueryBuilder select(List<GraphQLField> selections) {
            this.filters.add(new SelectionFieldFilter(selections));
            return this;
        }

        public String build(String request, String field, Set<String> params) {
            this.filters.add(new AllNonNullArgsFieldFilter());
            this.filters.add(new FieldDuplicationFilter());
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
