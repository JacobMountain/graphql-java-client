package com.jacobmountain;


import com.jacobmountain.dto.Episode;
import com.jacobmountain.dto.Review;
import com.jacobmountain.graphql.client.annotations.GraphQLArgument;
import com.jacobmountain.graphql.client.annotations.GraphQLClient;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;
import com.jacobmountain.graphql.client.annotations.GraphQLSubscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@GraphQLClient(
        schema = "Schema.gql",
        maxDepth = 5,
        nullChecking = true,
        reactive = true
)
public interface ReactiveStarWarsClient {

    @GraphQLQuery(value = "hero")
    com.jacobmountain.dto.Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery(value = "hero", name = "ReactiveHeroOptional")
    Optional<com.jacobmountain.dto.Character> getHeroOptional(@GraphQLArgument("hero") String id);

    @GraphQLQuery(value = "hero", name = "ReactiveHeroOptional")
    Mono<com.jacobmountain.dto.Character> getHeroPublisher(@GraphQLArgument("hero") String hero);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery("reviews")
    Flux<Review> getReviewFlux(Episode episode);

    @GraphQLQuery("reviews")
    Mono<List<Review>> getReviewListMono(Episode episode);

    @GraphQLSubscription("reviewAdded")
    Flux<Review> watchReviews(Episode episode);

}
