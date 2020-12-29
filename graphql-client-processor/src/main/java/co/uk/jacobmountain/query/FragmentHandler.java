package co.uk.jacobmountain.query;

import graphql.language.FieldDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
abstract class FragmentHandler {

    private final QueryGenerator queryGenerator;

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
    abstract Stream<String> handle(TypeDefinition<?> typeDefinition,
                                   Set<String> params,
                                   Set<String> visited,
                                   int depth,
                                   Set<String> argumentCollector);

    abstract String getFragments();

    protected Optional<String> expandChildren(String interfac, Set<String> params, Set<String> visited, int depth, Set<String> argumentCollector) {
        FieldDefinition definition = new FieldDefinition(interfac, new TypeName(interfac));
        return queryGenerator.generateQueryRec(interfac, definition, params, visited, depth, argumentCollector, this);
    }

}
