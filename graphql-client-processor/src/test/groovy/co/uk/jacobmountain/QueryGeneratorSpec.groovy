package co.uk.jacobmountain


import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
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

"""

    static final TypeDefinitionRegistry TDR = new SchemaParser().parse(SCHEMA)

    @Subject
    QueryGenerator generator = new QueryGenerator(TDR, 2)

    static boolean queriesAreEqual(String expected, String result) {
        log.info("Asserting equal: ")
        log.info("\t{}", expected)
        log.info("\t{}", result)
        expected == result
    }

    def "I can generate a simple query"(){
        when:
        def result = generator.generateQuery("string", false)

        then:
        queriesAreEqual("query String { string }", result)
    }

    def "I can generate a simple object query"(){
        when:
        def result = generator.generateQuery("obj", false)

        then:
        queriesAreEqual("query Obj { obj { nested __typename } }", result)
    }

    def "I can generate an object query with an argument"(){
        when:
        def result = generator.generateQuery("findObject", false)

        then:
        queriesAreEqual("query FindObject(\$id: String) { findObject(id: \$id) { nested __typename } }", result)
    }

    def "I can generate an object query for an interface"(){
        when:
        def result = generator.generateQuery("character", false)

        then:
        queriesAreEqual("query Character { character { id ... on Hero { name __typename } ... on Droid { name primaryFunction __typename } __typename } }", result)
    }


    def "I can generate a recursive query" (){
        given:
        QueryGenerator generator = new QueryGenerator(TDR, 5)

        when:
        def result = generator.generateQuery("hero", false)

        then:
        queriesAreEqual("query Hero { hero { name friends { name friends { name friends { name __typename } __typename } __typename } __typename } }", result)
    }

    def "I can generate a query with a non null arg"() {
        when:
        def result = generator.generateQuery("findObjectNonNullArg", false)

        then:
        queriesAreEqual("query FindObjectNonNullArg(\$id: String!) { findObjectNonNullArg(id: \$id) { nested __typename } }", result)
    }

}
