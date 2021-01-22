package com.jacobmountain.query.filters

import com.jacobmountain.graphql.client.query.FieldFilter
import com.jacobmountain.graphql.client.query.QueryContext
import com.jacobmountain.graphql.client.query.filters.SelectionFieldFilter
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.GraphQLFieldSelection
import spock.lang.Specification

import static java.util.Collections.emptySet

class SelectionFieldFilterSpec extends Specification {

    QueryContext contextWithField(String field) {
        def fieldDef = new Schema("""
        schema {
            query: Query
        }
        type Query {
            $field: String
        }
        """).findField(field)
                .orElse(null)
        new QueryContext(1, fieldDef, emptySet(), emptySet())
    }

    def "We can select which fields are selected"() {
        given:
        FieldFilter filter = new SelectionFieldFilter(
                selection.collect { new GraphQLFieldSelection(it) }
        )

        expect:
        filter.shouldAddField(contextWithField(field)) == should

        where:
        selection  | field | should
        ["a"]      | "a"   | true
        ["a", "b"] | "a"   | true
        ["a", "b"] | "b"   | true
        ["a", "b"] | "c"   | false
    }

}
