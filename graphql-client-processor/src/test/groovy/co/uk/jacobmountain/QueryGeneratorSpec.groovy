package co.uk.jacobmountain


import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import spock.lang.Specification
import spock.lang.Subject

class QueryGeneratorSpec extends Specification {

    static final String SCHEMA = """
schema {
    query: Query
}

type Query {
    string: String
    obj: Object
}

type Object {
    nested: String
}
"""

    static final TypeDefinitionRegistry TDR = new SchemaParser().parse(SCHEMA)

    @Subject
    QueryGenerator generator = new QueryGenerator(TDR, 2)

    static boolean queriesAreEqual(String expected, String result) {
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

}
