package co.uk.jacobmountain;


import co.uk.jacobmountain.dto.Episode;
import co.uk.jacobmountain.dto.Review;
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
    co.uk.jacobmountain.dto.Character getHero(@GraphQLArgument("hero") String id);

    @GraphQLQuery(value = "hero", request = "ReactiveHeroOptional")
    Optional<co.uk.jacobmountain.dto.Character> getHeroOptional(@GraphQLArgument("hero") String id);

    @GraphQLQuery(value = "hero", request = "ReactiveHeroOptional")
    Mono<co.uk.jacobmountain.dto.Character> getHeroPublisher(@GraphQLArgument("hero") String hero);

    @GraphQLQuery("reviews")
    List<Review> getReviews(Episode episode);

    @GraphQLQuery("reviews")
    Flux<Review> getReviewFlux(Episode episode);

    @GraphQLQuery("reviews")
    Mono<List<Review>> getReviewListMono(Episode episode);

    @GraphQLSubscription("reviewAdded")
    Flux<Review> watchReviews(Episode episode);

}
