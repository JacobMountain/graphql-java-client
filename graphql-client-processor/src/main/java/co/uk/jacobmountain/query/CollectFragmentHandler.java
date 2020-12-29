package co.uk.jacobmountain.query;

import co.uk.jacobmountain.utils.Schema;
import graphql.language.TypeDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class CollectFragmentHandler extends FragmentHandler {

    private final Schema schema;

    private final Map<String, String> fragments = new HashMap<>();

    public CollectFragmentHandler(Schema schema, QueryGenerator queryGenerator) {
        super(queryGenerator);
        this.schema = schema;
    }

    @Override
    public Stream<String> handle(TypeDefinition<?> typeDefinition, Set<String> params, Set<String> visited, int depth, Set<String> argumentCollector) {
        return schema.getTypesImplementing(typeDefinition)
                .map(interfac -> {
                    if (!fragments.containsKey(interfac)) {
                        expandChildren(interfac, params, visited, depth, argumentCollector)
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
