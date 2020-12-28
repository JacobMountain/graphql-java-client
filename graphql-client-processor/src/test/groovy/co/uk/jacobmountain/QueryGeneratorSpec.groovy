package co.uk.jacobmountain

import co.uk.jacobmountain.utils.Schema
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Subject

import static org.junit.Assert.assertEquals

@Slf4j
class QueryGeneratorSpec extends Specification {

    @Subject
    QueryGenerator generator

    static String clean(String query) {
        query.split("\n").collect { it.trim() }.join(" ").trim()
    }

    static List<String> split(String query) {
        return [
                query.find("(query|mutation|subscription)\\s*").trim(), // finds the type
                query.find("\\((\\\$\\w+:\\s+\\w+,?\\s*)+\\)"),         // finds the args
                query.find("\\{.*")                                     // finds the selections
        ]
    }

    static void assertArgs(String expected = "", String actual) {
        actual = actual ?: "()"
        expected = expected ?: "()"
        assertEquals("Args are incorrect", "(", actual.substring(0, 1))
        assertEquals("Args are incorrect", ")", actual.substring(actual.length() - 1))
        assertEquals(
                "Args are incorrect",
                expected.substring(1, expected.length() - 1).split(", ") as Set,
                actual.substring(1, expected.length() - 1).split(", ") as Set
        )
    }

    static void queriesAreEqual(String expected, String result) {
        log.info("Asserting equal: ")
        expected = clean(expected)
        result = clean(result)
        log.info("\t{}", expected)
        log.info("\t{}", result)
        def a = split(expected)
        def b = split(result)
        assertEquals("Type is incorrect", a[0], b[0])
        assertArgs(a[1], b[1])
        assertEquals("Fields are incorrect", a[2], b[2])
    }

    def givenSchema(String schema, int depth = 2) {
        println schema.stripIndent()
        generator = new QueryGenerator(new Schema(schema.stripIndent()), depth)
    }

    def givenQuery(String query, String types = "", int depth = 2) {
        givenSchema("""
        schema {
            query: Query
        }
        type Query {
            ${query}
        }
        ${types}
        """, depth)
    }

    def "I can query structured type fields"() {
        given:
        givenQuery("field: Field", """
        type Field implements ID {
            id: Int
            name: String
        }
        """)
        when:
        def result = generator.generateQuery(null, "field", [] as Set)

        then:
        queriesAreEqual("""
        query Field { 
            field { 
                id
                name
                __typename
            } 
        }
        """, result)
    }

    def "I can query scalar type fields"() {
        given:
        givenQuery("number: Int")
        when:
        def result = generator.generateQuery(null, "number", [] as Set)

        then:
        queriesAreEqual("""
        query Number { 
            number
        }
        """, result)
    }

    def "I can query enum fields"() {
        given:
        givenQuery("ep: Episode", """
        enum Episode {
            NEWHOPE
            EMPIRE
            JEDI
        }
        """)
        when:
        def result = generator.generateQuery(null, "ep", [] as Set)

        then:
        queriesAreEqual("""
        query Ep { 
            ep
        }
        """, result)
    }

    def "I can query nested enum fields"() {
        given:
        givenQuery("field: Field", """
        type Field implements ID {
            id: Int
            name: String
            number: Number
        }
        enum Number {
            ONE
            TWO
            THREE
        }
        """)
        when:
        def result = generator.generateQuery(null, "field", [] as Set)

        then:
        queriesAreEqual("""
        query Field { 
            field {
                id
                name
                number
                __typename
            }
        }
        """, result)
    }

    def "I can query for fields with args"() {
        given:
        givenQuery("field(${vars.split(", ").collect { it.substring(1) }.join(", ")}): Field", """
        type Field implements ID {
            id: Int
            name: String
        }
        """)
        when:
        def result = generator.generateQuery(null, "field", params as Set)

        then:
        queriesAreEqual("""
        query Field($vars) { 
            field($args) {
                id
                name
                __typename
            }
        }
        """, result)

        where:
        vars                      | args                   | params
        '$id: Int'                | 'id: $id'              | ["id"]
        '$id: Int, $name: String' | 'id: $id, name: $name' | ["id", "name"]
    }

    def "Recursive queries don't fail"() {
        given:
        givenQuery("friend: Person", """
        type Person {
            id: Int
            name: String
            friends: [Person]
        }
        """, 4)
        when:
        def result = generator.generateQuery(null, "friend", [] as Set)

        then:
        queriesAreEqual("""
        query Friend { 
            friend {
                id
                name
                friends {
                    id
                    name
                    friends {
                        id
                        name
                        __typename
                    }
                    __typename
                }
                __typename
            }
        }
        """, result)
    }

    def "I can generate a mutation"() {
        given:
        givenSchema("""
        schema {
            query: Query
            mutation: Mutation
        }
        type Query {}
        type Mutation {
            doAction: String
        }
        """)

        when:
        def result = generator.generateMutation(null, "doAction", [] as Set)
        then:
        queriesAreEqual("""
        mutation Friend {
            doAction
        }
        """, result)
    }

    def "I can generate a subscription"() {
        given:
        givenSchema("""
        schema {
            query: Query
            subscription: Subscription
        }
        type Query {}
        type Subscription {
            doSubscribe: String
        }
        """)

        when:
        def result = generator.generateSubscription(null, "doSubscribe", [] as Set)
        then:
        queriesAreEqual("""
        subscription Friend {
            doSubscribe
        }
        """, result)
    }

    def "I can query for interfaces"() {
        given:
        givenQuery("field: Named", """
        type Field implements Named {
            id: Int
            name: String
        }
        interface Named {
            name: String
        }
        """)
        when:
        def result = generator.generateQuery(null, "field", [] as Set)

        then:
        queriesAreEqual("""
        query Field { 
            field {
                name
                ... on Field { 
                    id
                    __typename 
                }
                __typename 
            }
        }
        """, result)
    }

}
