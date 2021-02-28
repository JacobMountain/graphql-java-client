package com.jacobmountain.query.filters

import com.jacobmountain.graphql.client.query.QueryContext
import com.jacobmountain.graphql.client.query.filters.AllNonNullArgsFieldFilter
import com.jacobmountain.graphql.client.utils.Schema
import spock.lang.Specification

class AllNonNullArgsFieldFilterSpec extends Specification {

    QueryContext contextWithField(String field, Collection<String> params) {
        def fieldDef = new Schema("""
        schema {
            query: Query
        }
        type Query {
            field($field): String
        }
        """).findField("field")
                .orElse(null)
        new QueryContext(null, 0, fieldDef, new HashSet<String>(params), new HashSet<String>())
    }

    def "We can filter fields based upon whether or not we can supply all of their non-null arguments"() {
        expect:
        should == new AllNonNullArgsFieldFilter().shouldAddField(contextWithField(graphQLArgs, methodAgs))

        where:
        graphQLArgs              | methodAgs  | should
        "a: String!, b: String"  | ["a"]      | true
        "a: String!, b: String"  | ["a", "b"] | true
        "a: String!, b: String!" | ["a", "b"] | true
        "a: String!, b: String!" | []         | false
        "a: String, b: String!"  | []         | false
        "a: String, b: String"   | []         | true
    }

}
