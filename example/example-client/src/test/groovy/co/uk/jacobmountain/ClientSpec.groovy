package co.uk.jacobmountain

import co.uk.jacobmountain.dto.MatchResult
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest(
        classes = ExampleApplication,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
class ClientSpec extends Specification {

    @Subject
    MatchResultsClient client = new MatchResultsClientGraph(new ExampleFetcher("http://localhost:8080/graph"))

    def "I can get a query with an argument"(){
        when:
        def result = client.getResult(1)

        then:
        result != null
        UUID.fromString(result.home.team.id)
        result.home.points > 0
        UUID.fromString(result.away.team.id)
        result.away.points > 0
    }

}
