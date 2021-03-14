package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.ReactiveFetcher
import com.jacobmountain.graphql.client.ReactiveSubscriber
import com.jacobmountain.graphql.client.TypeMapper
import com.jacobmountain.graphql.client.dto.Response
import com.jacobmountain.graphql.client.modules.AbstractStage
import com.jacobmountain.graphql.client.modules.ClientDetails
import com.jacobmountain.graphql.client.modules.ReactiveQueryStage
import com.jacobmountain.graphql.client.query.QueryGenerator
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.MethodDetails
import org.reactivestreams.Publisher
import spock.lang.Specification

import static com.jacobmountain.client.modules.CodeBlockUtils.renderBlocks

class ReactiveQueryStageSpec extends Specification {

    QueryGenerator.QueryBuilder queryBuilder = Mock(QueryGenerator.QueryBuilder) {
        select(_ as List) >> { queryBuilder }
        maxDepth(_) >> { queryBuilder }
        build(_, _, _) >> { "query" }
    }

    QueryGenerator generator = Mock(QueryGenerator)

    ReactiveQueryStage stage = new ReactiveQueryStage(
            generator,
            new Schema("""
            schema {
                query: Query
                mutation: Mutation
                subscription: Subscription
            }
            type Query { }
            type Mutation { }
            type Subscription { }
            """),
            new TypeMapper("com.test"),
            "com.test"
    )

    def "We can generate the code for a query"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .selection([])
                .build())

        then: "the stage fetches the query from the fetcher"
        1 * generator.query() >> queryBuilder
        renderBlocks(blocks) == """${Publisher.class.getName()}<${Response.class.getName()}<com.test.Query, Error>> thing = fetcher.query("query", null);"""
    }

    def "We can generate the code for a mutation"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .selection([])
                .mutation(true)
                .build())

        then: "the stage fetches the mutation from the fetcher"
        1 * generator.mutation() >> queryBuilder
        renderBlocks(blocks) == """${Publisher.class.getName()}<${Response.class.getName()}<com.test.Mutation, Error>> thing = fetcher.mutate("query", null);"""
    }

    def "We can generate the code for a subscription"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .selection([])
                .subscription(true)
                .build())

        then: "the stage fetches the subscription from the fetcher"
        1 * generator.subscription() >> queryBuilder
        renderBlocks(blocks) == """${Publisher.class.getName()}<${Response.class.getName()}<com.test.Subscription, Error>> thing = subscriber.subscribe("query", null);"""
    }

    def "The reactive client class is generated with the correct member variables"() {
        when:
        def members = stage.getMemberVariables(
                ClientDetails.builder()
                        .requiresFetcher(requiresFetcher)
                        .requiresSubscriber(requiresSubscriber)
                        .build()
        )

        then:
        members.size() == (requiresFetcher ? 1 : 0) + (requiresSubscriber ? 1 : 0)
        getMemberVariable("fetcher", members)
                .map { it -> it.type.toString() == "${ReactiveFetcher.class.getName()}<com.test.Query, com.test.Mutation, Error>" }
                .orElse(true)
        getMemberVariable("subscriber", members)
                .map { it -> it.type.toString() == "${ReactiveSubscriber.class.getName()}<com.test.Subscription, Error>" }
                .orElse(true)

        where:
        requiresFetcher | requiresSubscriber
        true            | true
        true            | false
        false           | true
        false           | false
    }

    static Optional<AbstractStage.MemberVariable> getMemberVariable(String name, List<AbstractStage.MemberVariable> variables) {
        Optional.ofNullable(variables.find { it.name == name })
    }

}
