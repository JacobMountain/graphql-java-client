package co.uk.jacobmountain;

import co.uk.jacobmountain.exceptions.FieldNotFoundException;
import co.uk.jacobmountain.utils.Schema;
import co.uk.jacobmountain.utils.StringUtils;
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
        FragmentHandler fragments = new CollectFragmentHandler();

        String inner = generateQueryRec(field, definition, params, new HashSet<>(), 1, args, fragments).orElseThrow(RuntimeException::new);

        String collect = String.join(", ", args);

        if (!args.isEmpty()) {
            collect = "(" + collect + ")";
        }

        return generateQueryName(request, type, field) + collect + " { " + inner + " } " + fragments.getFragments();
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

    private Optional<String> generateQueryRec(String alias,
                                              FieldDefinition field,
                                              Set<String> params,
                                              Set<String> previouslyVisited,
                                              int depth,
                                              Set<String> argumentCollector,
                                              FragmentHandler fragments) {
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
                        .map(definition -> generateQueryRec(definition.getName(), definition, params, new HashSet<>(), depth + 1, argumentCollector, fragments))
                        .filter(Optional::isPresent)
                        .map(Optional::get),
                fragments.handle(typeDefinition, params, visited, depth, argumentCollector)
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
     * Takes a type definition and returns a stream of types that implement it
     *
     * @param typeDefinition the possible InterfaceTypeDefinition
     * @return
     */
    private Stream<String> getTypesImplementing(TypeDefinition<?> typeDefinition) {
        if (!(typeDefinition instanceof InterfaceTypeDefinition)) {
            return Stream.empty();
        }
        return schema.types()
                .values()
                .stream()
                .filter(it -> it instanceof ObjectTypeDefinition)
                .map(it -> (ObjectTypeDefinition) it)
                .filter(it -> it.getImplements().stream().anyMatch(ty -> ((TypeName) ty).getName().equals(typeDefinition.getName())))
                .map((ObjectTypeDefinition impl) -> ((NamedNode<?>) impl).getName());
    }

    interface FragmentHandler {

        /**
         * Takes a possible interface type definition, and converts it into an interface selection
         *
         * @param typeDefinition    the TypeDefinition that may be an interface
         * @param params            the whitelist of params to include
         * @param visited           the blacklist of visited fields
         * @param depth             the current depth of the query
         * @param argumentCollector the current collected list of arguments
         * @return a stream of selections (or fragment spreads)
         */
        Stream<String> handle(TypeDefinition<?> typeDefinition,
                              Set<String> params,
                              Set<String> visited,
                              int depth,
                              Set<String> argumentCollector);

        String getFragments();

    }

    class CollectFragmentHandler implements FragmentHandler {

        Map<String, String> fragments = new HashMap<>();

        @Override
        public Stream<String> handle(TypeDefinition<?> typeDefinition, Set<String> params, Set<String> visited, int depth, Set<String> argumentCollector) {
            return getTypesImplementing(typeDefinition)
                    .map(interfac -> {
                        if (!fragments.containsKey(interfac)) {
                            generateQueryRec(interfac, new FieldDefinition(interfac, new TypeName(interfac)), params, visited, depth, argumentCollector, this)
                                    .map(query -> "fragment " + interfac + " on " + query)
                                    .ifPresent(selection -> fragments.put(interfac, selection));
                        }
                        return "..." + interfac;
                    });
        }

        @Override
        public String getFragments() {
            return String.join(" ", fragments.values());
        }
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

    class SpreadFragmentHandler implements FragmentHandler {

        @Override
        public Stream<String> handle(TypeDefinition<?> typeDefinition, Set<String> params, Set<String> visited, int depth, Set<String> argumentCollector) {
            return getTypesImplementing(typeDefinition)
                    .map(interfac -> generateQueryRec(interfac, new FieldDefinition(interfac, new TypeName(interfac)), params, visited, depth, argumentCollector, this))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(query -> "... on " + query);
        }

        @Override
        public String getFragments() {
            return "";
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
