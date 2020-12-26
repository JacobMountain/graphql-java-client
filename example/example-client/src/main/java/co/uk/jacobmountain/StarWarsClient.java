package co.uk.jacobmountain;


import co.uk.jacobmountain.domain.Episode;
import co.uk.jacobmountain.domain.LengthUnit;
import co.uk.jacobmountain.domain.Review;
import co.uk.jacobmountain.domain.ReviewInput;

import java.util.List;
import java.util.Optional;

@GraphQLClient(
        schema = "Schema.gql",
        maxDepth = 5,
        nullChecking = true,
        dtoPackage = "domain"
)
public interface StarWarsClient {

    @GraphQLQuery("hero")
    co.uk.jacobmountain.domain.Character getHero(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery(value = "hero", request = "HeroSummary")
    co.uk.jacobmountain.domain.Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery("hero")
    Optional<co.uk.jacobmountain.domain.Character> getHeroOptional(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery(value = "createReview", mutation = true)
    Review createReview(Episode episode, @GraphQLArgument("review") ReviewInput input);

}
