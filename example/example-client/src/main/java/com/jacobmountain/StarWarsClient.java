package com.jacobmountain;


import com.jacobmountain.dto.Episode;
import com.jacobmountain.dto.LengthUnit;
import com.jacobmountain.dto.Review;
import com.jacobmountain.dto.ReviewInput;
import com.jacobmountain.graphql.client.annotations.GraphQLArgument;
import com.jacobmountain.graphql.client.annotations.GraphQLClient;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;

import java.util.List;
import java.util.Optional;

@GraphQLClient(
        schema = "Schema.gql",
        maxDepth = 5,
        nullChecking = true
)
public interface StarWarsClient {

    @GraphQLQuery(value = "hero", request = "HeroByEpisode")
    com.jacobmountain.dto.Character getHero(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery(value = "hero", request = "HeroSummary")
    com.jacobmountain.dto.Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery("hero")
    Optional<com.jacobmountain.dto.Character> getHeroOptional(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery(value = "createReview", mutation = true)
    Review createReview(Episode episode, @GraphQLArgument("review") ReviewInput input);

}
