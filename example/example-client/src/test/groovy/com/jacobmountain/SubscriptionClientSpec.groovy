package com.jacobmountain

import com.jacobmountain.dto.Error
import com.jacobmountain.dto.Mutation
import com.jacobmountain.dto.Query
import com.jacobmountain.dto.ReviewInput
import com.jacobmountain.fetchers.WebSocketSubscriber
import com.jacobmountain.graphql.client.web.spring.RestTemplateFetcher
import com.jacobmountain.resolvers.dto.Episode
import com.jacobmountain.service.DefaultService
import com.jacobmountain.service.StarWarsService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import reactor.core.publisher.Flux
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
    StarWarsClient client

    @SpringBean
    StarWarsService service = Spy(DefaultService)

    def setup() {
        client = new StarWarsClientGraph(
                new RestTemplateFetcher("http://localhost:$port", Query, Mutation, Error),
                new WebSocketSubscriber("ws://localhost:$port/subscriptions")
        )
    }

    static ReviewInput randomReviewInput() {
        ReviewInput review = new ReviewInput()
        review.setCommentary("")
        review.setStars(new Random().nextInt(5))
        review
    }

    static def every(int ms, int times, Closure close) {
        Flux.interval(Duration.ofMillis(ms))
                .limitRequest(times)
                .subscribe(close)
    }

    def "I can subscribe to a subscription"() {
        when:
        def reviews = []
        client.watchReviews(com.jacobmountain.dto.Episode.JEDI) { a ->
            reviews.add(a)
        }

        and:
        Thread.sleep(100)
        int id = 0
        every(100, 5) { service.createRandomReview(id++, Episode.JEDI) }
        Thread.sleep(600)

        then:
        def list = reviews
        list.size() == 5
    }

}
