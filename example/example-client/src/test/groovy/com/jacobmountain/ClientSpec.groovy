package com.jacobmountain

import com.jacobmountain.dto.LengthUnit
import com.jacobmountain.dto.ReviewInput
import com.jacobmountain.fetchers.RestTemplateFetcher
import com.jacobmountain.fetchers.SpyFetcher
import com.jacobmountain.resolvers.dto.Episode
import com.jacobmountain.resolvers.dto.Review
import com.jacobmountain.service.DefaultService
import com.jacobmountain.service.StarWarsService
import com.jacobmountain.util.Assert
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import spock.lang.Specification
import spock.lang.Subject

import static com.jacobmountain.dto.Episode.EMPIRE

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
        fetcher = new SpyFetcher(new RestTemplateFetcher("http://localhost:$port"))
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

    static ReviewInput randomReviewInput() {
        ReviewInput review = new ReviewInput();
        review.setCommentary("");
        review.setStars(new Random().nextInt(5));
        review
    }

    static Review fromReview(ReviewInput review, Episode episode) {
        Review ret = new Review()
        ret.episode = episode
        ret.commentary = review.commentary
        ret.stars = review.stars
        ret
    }

    def "I can mutate"() {
        given:
        Review saved
        def expected = randomReviewInput()

        when:
        client.createReview(com.jacobmountain.dto.Episode.JEDI, expected)

        then:
        1 * service.createReview(_, _) >> { args -> saved = args[1] }
        saved == fromReview(expected, Episode.JEDI)
    }

}
