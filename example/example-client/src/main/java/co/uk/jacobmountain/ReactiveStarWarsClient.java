package co.uk.jacobmountain;

import co.uk.jacobmountain.dto.Episode;
import co.uk.jacobmountain.dto.LengthUnit;
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

    @GraphQLQuery("hero")
    co.uk.jacobmountain.dto.Character getHero(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("hero")
    Optional<co.uk.jacobmountain.dto.Character> getHeroOptional(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("hero")
    Mono<co.uk.jacobmountain.dto.Character> getHeroAsync(Episode episode, int first, String after, LengthUnit unit);

    @GraphQLQuery("reviews")
    Flux<Review> getReviewsFlux(Episode episode);

    @GraphQLQuery("reviews")
    Mono<List<Review>> getReviewsAsync(Episode episode);

}
