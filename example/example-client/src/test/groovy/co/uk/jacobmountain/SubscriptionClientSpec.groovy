package co.uk.jacobmountain

import co.uk.jacobmountain.dto.Episode
import co.uk.jacobmountain.dto.ReviewInput
import co.uk.jacobmountain.fetchers.WebClientFetcher
import co.uk.jacobmountain.fetchers.WebSocketSubscriber
import co.uk.jacobmountain.service.DefaultService
import co.uk.jacobmountain.service.StarWarsService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration

@SpringBootTest(
        classes = ExampleApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class SubscriptionClientSpec extends Specification {

    @LocalServerPort
    int port

    @Subject
    ReactiveStarWarsClient client

    @SpringBean
    StarWarsService service = Spy(DefaultService)

    def setup() {
        Hooks.onOperatorDebug()
        client = new ReactiveStarWarsClientGraph(
                new WebClientFetcher("http://localhost:$port/graph"),
                new WebSocketSubscriber("ws://localhost:$port/subscriptions")
        )
    }

    static ReviewInput randomReviewInput() {
        ReviewInput review = new ReviewInput()
        review.setCommentary("")
        review.setStars(new Random().nextInt(5))
        review
    }

    static def every(int ms, Closure close) {
        Flux.interval(Duration.ofMillis(ms)).subscribe(close)
    }

    def "I can subscribe to a subscription"() {
        when:
        def reviews = client.watchReviews(Episode.JEDI).limitRequest(5)

        and:
        every(100) { i -> service.createRandomReview(co.uk.jacobmountain.resolvers.dto.Episode.JEDI) }

        then:
        def list = reviews.collectList().block()
        list.size() == 5
    }

}
