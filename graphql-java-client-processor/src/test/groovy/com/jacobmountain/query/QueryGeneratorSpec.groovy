package com.jacobmountain.query

import com.jacobmountain.graphql.client.query.QueryGenerator
import com.jacobmountain.graphql.client.utils.Schema
import spock.lang.Specification

class QueryGeneratorSpec extends Specification {

    static QueryGenerator givenQuery(String query, String types) {
        new QueryGenerator(new Schema("""
            schema {
                query: Query
            }
            type Query {
                $query
            }
            $types
        """))
    }

    def "I can generate a query with a fragment"() {
        given:
        def generator = givenQuery("hero: Hero","""
            type Hero {
                id: String
                name: String
            }
        """)

        when:
        def query = generator.query()
                .build(null, "hero", [] as Set)

        then:
        query == "query Hero { hero { id name __typename } }"
    }

    def "I can generate a complex query with a fragment"() {
        given:
        def generator = givenQuery("hero: Hero","""
            type Hero {
                id: String
                name: String
                ships: [Starship!]!
            }
            type Starship {
                id: String!
                name: String!
                length(unit: LengthUnit = METER): Float
                coordinates: [[Float!]!]
            }
            enum LengthUnit {
                METER
                FOOT
            }
        """)

        when:
        def query = generator.query()
                .build(null, "hero", [] as Set)

        then:
        query == "query Hero { hero { id name ships { id name length coordinates __typename } __typename } }"
    }

    def "I can generate a recursive query with a fragment"() {
        given:
        def generator = givenQuery("hero: Hero","""
            type Hero {
                id: String
                name: String
                friends: [Hero!]!
            }
        """)

        when:
        def query = generator.query()
                .maxDepth(3)
                .build(null, "hero", [] as Set)

        then:
        query == "query Hero { hero { id name friends { id name friends { id name __typename } __typename } __typename } }"
    }

}
