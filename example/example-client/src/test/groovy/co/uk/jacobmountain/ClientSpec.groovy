package co.uk.jacobmountain

import co.uk.jacobmountain.dto.LengthUnit
import co.uk.jacobmountain.resolvers.dto.Episode
import co.uk.jacobmountain.service.DefaultService
import co.uk.jacobmountain.service.StarWarsService
import co.uk.jacobmountain.util.Assert
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import spock.lang.Specification
import spock.lang.Subject

import static co.uk.jacobmountain.dto.Episode.EMPIRE

@SpringBootTest(
        classes = ExampleApplication,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ClientSpec extends Specification {

    @LocalServerPort
    int port

    @Subject
    StarWarsClient client

    SpyFetcher fetcher

    @SpringBean
    StarWarsService service = Spy(DefaultService)

    def setup() {
        fetcher = new SpyFetcher(new ExampleFetcher("http://localhost:$port"))
        client = new StarWarsClientGraph(fetcher)
    }

    def "I can get a query with an argument"() {
        given:
        def expected = DefaultService.randomHuman()
        service.getHero(Episode.EMPIRE) >> expected

        when:
        def result = client.getHero(EMPIRE, 0, "1", LengthUnit.METER)

        then:
        result != null
        Assert.assertEquals(expected, result)
    }

    def "I can get a query a list"() {
        given:
        def expected = (1..3).collect { DefaultService.randomReview(Episode.EMPIRE) }
        service.getReviews(Episode.EMPIRE) >> expected

        when:
        def result = client.getReviews(EMPIRE)

        then:
        result != null
        Assert.assertEquals(expected, result)
    }

    def "I can get a query and wrap it in an Optional"() {
        given:
        def expected = DefaultService.randomHuman()
        service.getHero(Episode.EMPIRE) >> expected

        when:
        def result = client.getHeroOptional(EMPIRE, 0, "1", LengthUnit.METER)

        then:
        result != null
        Assert.assertEquals(expected, result.orElse(null))
    }

    def "I can get a query with a parameter named differently to its argument"() {
        given:
        def expected = service.getFriend("1000")

        when:
        def result = client.getHero("1000")

        then:
        Assert.assertEquals(expected, result)
    }

    def "A NPE is thrown for null arguments before the request is made"() {
        when:
        client.getReviews(null)

        then: "No HTTP requests should occur"
        !fetcher.hasInteractions()

        and: "A NPE should be thrown"
        thrown(NullPointerException)
    }


}
