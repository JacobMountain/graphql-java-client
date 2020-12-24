package co.uk.jacobmountain;


import co.uk.jacobmountain.dto.Episode;
import co.uk.jacobmountain.dto.LengthUnit;
import co.uk.jacobmountain.dto.Review;

import java.util.List;
import java.util.Optional;

@GraphQLClient(
        schema = "Schema.gql",
        mapping = {

        },
        maxDepth = 5,
        nullChecking = true
)
public interface StarWarsClient {

    @GraphQLQuery("hero")
    co.uk.jacobmountain.dto.Character getHero(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery(value = "hero", request = "HeroSummary")
    co.uk.jacobmountain.dto.Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery("hero")
    Optional<co.uk.jacobmountain.dto.Character> getHeroOptional(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

}
