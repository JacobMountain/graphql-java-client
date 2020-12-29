package co.uk.jacobmountain.query

import co.uk.jacobmountain.query.QueryGenerator
import co.uk.jacobmountain.utils.Schema
import groovy.util.logging.Slf4j
import spock.lang.Specification
import spock.lang.Subject

import static co.uk.jacobmountain.utils.QueryAssertion.assertQueriesAreEqual

@Slf4j
class QueryGeneratorSpec extends Specification {

    @Subject
    QueryGenerator generator

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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
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
        assertQueriesAreEqual("""
        subscription Friend {
            doSubscribe
        }
        """, result)
    }

    def "I can query for interfaces"() {
        given:
        givenQuery("named: Named", """
        type Field implements Named {
            id: Int
            name: String
        }
        interface Named {
            name: String
        }
        """)
        when:
        def result = generator.generateQuery(null, "named", [] as Set)

        then:
        assertQueriesAreEqual("""
        query Field { 
            named {
                name
                ...field
                __typename 
            }
        }
        fragment field on Field {
            id
            __typename
        }
        """, result)
    }

    def "We should add nested fields of the same name (a problem potentially caused by interface recursion)"() {
        given:
        givenQuery("field: Field", """
        type Field {
            id: Int
            nested: Nested
        }
        type Nested {
            nested: String
        }
        """, 5)
        when:
        def result = generator.generateQuery(null, "field", [] as Set)

        then:
        assertQueriesAreEqual("""
        query Field { 
            field {
                id
                nested {
                    nested
                    __typename 
                }
                __typename 
            }
        }
        """, result)
    }

}
