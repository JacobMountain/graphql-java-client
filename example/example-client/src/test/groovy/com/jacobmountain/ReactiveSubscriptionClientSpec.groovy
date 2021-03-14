package com.jacobmountain

import com.jacobmountain.dto.Error
import com.jacobmountain.dto.Mutation
import com.jacobmountain.dto.Query
import com.jacobmountain.dto.ReviewInput
import com.jacobmountain.fetchers.ReactiveWebSocketSubscriber
import com.jacobmountain.graphql.client.web.spring.WebClientFetcher
import com.jacobmountain.resolvers.dto.Episode
import com.jacobmountain.service.DefaultService
import com.jacobmountain.service.StarWarsService
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
class ReactiveSubscriptionClientSpec extends Specification {

    @LocalServerPort
    int port

    @Subject
    ReactiveStarWarsClient client

    @SpringBean
    StarWarsService service = Spy(DefaultService)

    def setup() {
        Hooks.onOperatorDebug()
        client = new ReactiveStarWarsClientGraph(
                new WebClientFetcher<Query, Mutation, Error>("http://localhost:$port/graph", Query, Mutation, Error),
                new ReactiveWebSocketSubscriber("ws://localhost:$port/subscriptions")
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
        def reviews = client.watchReviews(com.jacobmountain.dto.Episode.JEDI).limitRequest(5)

        and:
        int id = 0
        every(100) { i -> service.createRandomReview(id++, Episode.JEDI) }

        then:
        def list = reviews.collectList().block()
        list.size() == 5
    }

}
