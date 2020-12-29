package co.uk.jacobmountain.query;

import co.uk.jacobmountain.utils.Schema;
import graphql.language.TypeDefinition;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

class SpreadFragmentHandler extends FragmentHandler {

    private final Schema schema;

    public SpreadFragmentHandler(Schema schema, QueryGenerator queryGenerator) {
        super(queryGenerator);
        this.schema = schema;
    }

    @Override
    public Stream<String> handle(TypeDefinition<?> typeDefinition, Set<String> params, Set<String> visited, int depth, Set<String> argumentCollector) {
        return schema.getTypesImplementing(typeDefinition)
                .map(interfac -> expandChildren(interfac, params, visited, depth, argumentCollector))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(query -> "... on " + query);
    }

    @Override
    public String getFragments() {
        return "";
    }

}
