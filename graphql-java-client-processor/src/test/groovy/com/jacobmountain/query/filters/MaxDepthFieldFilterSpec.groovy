package com.jacobmountain.query.filters

import com.jacobmountain.graphql.client.query.filters.FieldFilter
import com.jacobmountain.graphql.client.query.QueryContext
import com.jacobmountain.graphql.client.query.filters.MaxDepthFieldFilter
import spock.lang.Specification

import static java.util.Collections.emptySet

class MaxDepthFieldFilterSpec extends Specification {

    static QueryContext newContextWithDepth(int depth) {
        new QueryContext(null, depth, null, emptySet())
    }

    def "Max depth filter doesn't filter out fields under it's max depth"() {
        given:
        FieldFilter filter = new MaxDepthFieldFilter(maxDepth)

        expect:
        filter.shouldAddField(newContextWithDepth(depth))

        where:
        maxDepth | depth
        2        | 1
        2        | 2
        5        | 4
    }

    def "Max depth filters out fields over it's max depth"() {
        given:
        FieldFilter filter = new MaxDepthFieldFilter(maxDepth)

        expect:
        !filter.shouldAddField(newContextWithDepth(depth))

        where:
        maxDepth | depth
        1        | 5
        3        | 4
        5        | 10
    }

}
