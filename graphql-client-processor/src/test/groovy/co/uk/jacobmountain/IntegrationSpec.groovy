package co.uk.jacobmountain

import co.uk.jacobmountain.dto.*
import spock.lang.Specification
import spock.lang.Unroll

class IntegrationSpec extends Specification {

    Fetcher<Query, Void, Void> fetcher = Mock(Fetcher)

    Client graph = new ClientGraph(fetcher)

    static def newCharacter(int id, String name) {
        def character = new Human()
        character.id = id
        character.name = name
        character
    }
    static def newDroid(int id, String name) {
        def character = new Droid()
        character.id = id
        character.name = name
        character
    }

    def "Can query for data with no args"() {
        given:
        fetcher.query("query Hero { hero { id name friends { id name ... on Human { id name totalCredits __typename } ... on Droid { id name primaryFunction __typename } __typename } ... on Human { id name friends { id name ... on Human { id name totalCredits __typename } ... on Droid { id name primaryFunction __typename } __typename } totalCredits __typename } ... on Droid { id name friends { id name ... on Human { id name totalCredits __typename } ... on Droid { id name primaryFunction __typename } __typename } primaryFunction __typename } __typename } }", null) >> Response.builder()
                .data(new Query() {
                    @Override
                    Character getHero() {
                        return newCharacter(1, "Obi Wan")
                    }
                })
                .build()

        when:
        def hero = graph.getHero()

        then:
        hero.id == 1
        hero.name == "Obi Wan"
    }

    @Unroll
    def "Can query for data with args (#name)"() {
        given:
        def args = new DroidArguments()
        args.id = id
        def droid = newDroid(id, name)
        fetcher.query("query Droid(\$id: ID!) { droid(id: \$id) { id name friends { id name ... on Human { id name totalCredits __typename } ... on Droid { id name primaryFunction __typename } __typename } primaryFunction __typename } }", args) >> Response.builder()
                .data(new Query() {
                    @Override
                    Droid getDroid() {
                        return droid
                    }
                })
                .build()

        when:
        def hero = graph.getDroid(id)

        then:
        hero.id == id
        hero.name == name

        where:
        id | name
        2  | "R2-D2"
        3  | "C3P0"
    }

}
