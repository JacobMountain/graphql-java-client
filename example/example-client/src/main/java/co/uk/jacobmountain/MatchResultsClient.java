package co.uk.jacobmountain;


import co.uk.jacobmountain.dto.MatchResult;

import java.util.List;

@GraphQLClient(
        schema = "Schema.gql",
        mapping = {
                @GraphQLClient.Scalar(from = "ID", to = String.class)
        },
        maxDepth = 5
)
public interface MatchResultsClient {

    @GraphQLQuery("results")
    List<MatchResult> getResults();

    @GraphQLQuery("result")
    MatchResult getResult(int id);

}
