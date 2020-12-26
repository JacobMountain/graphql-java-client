package co.uk.jacobmountain

import co.uk.jacobmountain.utils.Schema
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Subject

@Slf4j
class QueryGeneratorSpec extends Specification {

    static final String SCHEMA = """
schema {
    query: Query
}

type Query {
    string: String
    obj: Object
    findObject(id: String): Object
    findObjectNonNullArg(id: String!): Object
    character: [ID]
    hero: Hero
    enum: Episode
}

type Object {
    nested: String
}

interface ID {
    id: int
}

type Hero implements ID {
    name: string
    friends: [Hero]
}

type Droid implements ID {
    name: string
    primaryFunction: String
}

enum Episode {
    # Star Wars Episode IV: A New Hope, released in 1977.
    NEWHOPE
    # Star Wars Episode V: The Empire Strikes Back, released in 1980.
    EMPIRE
    # Star Wars Episode VI: Return of the Jedi, released in 1983.
    JEDI
}
"""

    @Subject
    QueryGenerator generator = new QueryGenerator(new Schema(SCHEMA), 2)

    static boolean queriesAreEqual(String expected, String result) {
        log.info("Asserting equal: ")
        log.info("\t{}", expected)
        log.info("\t{}", result)
        expected == result
    }

    def "I can generate a simple query"(){
        when:
        def result = generator.generateQuery(null, "string", [] as Set, false)

        then:
        queriesAreEqual("query String { string }", result)
    }

    def "I can generate a simple object query"(){
        when:
        def result = generator.generateQuery(null, "obj", [] as Set, false)

        then:
        queriesAreEqual("query Obj { obj { nested __typename } }", result)
    }

    def "I can generate an object query with an argument"(){
        when:
        def result = generator.generateQuery(null, "findObject", ["id"] as Set, false)

        then:
        queriesAreEqual("query FindObject(\$id: String) { findObject(id: \$id) { nested __typename } }", result)
    }

    def "I can generate an object query for an interface"(){
        when:
        def result = generator.generateQuery(null, "character", [] as Set, false)

        then:
        queriesAreEqual("query Character { character { id ... on Hero { name __typename } ... on Droid { name primaryFunction __typename } __typename } }", result)
    }


    def "I can generate a recursive query" (){
        given:
        QueryGenerator generator = new QueryGenerator(new Schema(SCHEMA), 5)

        when:
        def result = generator.generateQuery(null, "hero", [] as Set, false)

        then:
        queriesAreEqual("query Hero { hero { name friends { name friends { name friends { name __typename } __typename } __typename } __typename } }", result)
    }

    def "I can generate a query with a non null arg"() {
        when:
        def result = generator.generateQuery(null, "findObjectNonNullArg", ["id"] as Set, false)

        then:
        queriesAreEqual("query FindObjectNonNullArg(\$id: String!) { findObjectNonNullArg(id: \$id) { nested __typename } }", result)
    }

    def "Enums"() {
        when:
        def result = generator.generateQuery(null, "enum", [] as Set, false)

        then:
        queriesAreEqual("query Enum { enum }", result)
    }

}
