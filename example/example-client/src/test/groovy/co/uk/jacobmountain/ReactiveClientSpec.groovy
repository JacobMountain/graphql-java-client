package co.uk.jacobmountain


import co.uk.jacobmountain.fetchers.WebClientFetcher
import co.uk.jacobmountain.fetchers.WebSocketSubscriber
import co.uk.jacobmountain.resolvers.dto.Episode
import co.uk.jacobmountain.service.DefaultService
import co.uk.jacobmountain.service.StarWarsService
import co.uk.jacobmountain.util.Assert
import org.junit.jupiter.api.Assertions
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import spock.lang.Specification
import spock.lang.Subject

import static co.uk.jacobmountain.dto.Episode.EMPIRE
import static org.awaitility.Awaitility.await

@SpringBootTest(
        classes = ExampleApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ReactiveClientSpec extends Specification {

    @LocalServerPort
    int port

    @Subject
    ReactiveStarWarsClient client

    @SpringBean
    StarWarsService service = Spy(DefaultService)

    def setup() {
        client = new ReactiveStarWarsClientGraph(new WebClientFetcher("http://localhost:$port/graph"), new WebSocketSubscriber("http://localhost:$port/subscriptions"))
    }

    def "The client blocks if the response isn't a publisher"() {
        given:
        def luke = service.getFriend("1000")

        when:
        def response = client.getHero("1000")

        then:
        Assert.assertEquals(luke, response)

        when:
        response = client.getHeroOptional("1000").orElse(null)

        then:
        Assert.assertEquals(luke, response)
    }

    def "The client doesn't block for publishers"() {
        given:
        def luke = service.getFriend("1000")

        when:
        def response
        client.getHeroPublisher("1000").subscribe { character -> response = character }

        then:
        Assertions.assertNull(response)

        await().untilAsserted {
            Assert.assertEquals(luke, response)
        }

    }

    def "The client can unwrap to Fluxs"() {
        given:
        def expected = (1..3).collect { DefaultService.randomReview(Episode.EMPIRE) }
        service.getReviews(Episode.EMPIRE) >> expected

        when:
        def result = client.getReviews(EMPIRE)

        then:
        Assert.assertEquals(expected, result)

        when:
        result = client.getReviewFlux(EMPIRE).collectList().block()

        then:
        Assert.assertEquals(expected, result)

        when:
        result = client.getReviewListMono(EMPIRE).block()

        then:
        Assert.assertEquals(expected, result)
    }

}
