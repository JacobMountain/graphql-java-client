package com.jacobmountain;


import com.jacobmountain.dto.Character;
import com.jacobmountain.dto.*;

import java.util.List;
import java.util.Optional;

@GraphQLClient(
        schema = "Schema.gql",
        maxDepth = 5,
        nullChecking = true
)
public interface StarWarsClient {

    @GraphQLQuery(value = "hero", request = "HeroByEpisode")
    Character getHero(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery(value = "hero", request = "HeroSummary")
    Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery("hero")
    Optional<Character> getHeroOptional(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery(value = "createReview", mutation = true)
    Review createReview(Episode episode, @GraphQLArgument("review") ReviewInput input);

}
