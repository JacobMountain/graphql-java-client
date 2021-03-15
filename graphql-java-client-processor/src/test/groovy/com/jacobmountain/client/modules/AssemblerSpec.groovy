package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.*
import com.jacobmountain.graphql.client.code.Assembler
import com.jacobmountain.graphql.client.code.MemberVariable
import com.jacobmountain.graphql.client.code.blocking.BlockingAssembler
import com.jacobmountain.graphql.client.code.reactive.ReactiveAssembler
import com.jacobmountain.graphql.client.query.QueryGenerator
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.ClientDetails
import spock.lang.Specification

class AssemblerSpec extends Specification {

    def "The reactive client class is generated with the correct member variables"() {
        given:
        Assembler assembler = new ReactiveAssembler(
                Mock(QueryGenerator),
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
                new TypeMapper("com.test")
        )

        when:
        def members = assembler.getMemberVariables(
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

    def "The default client class is generated with the correct member variables"() {
        given:
        Assembler assembler = new BlockingAssembler(
                Mock(QueryGenerator),
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
                new TypeMapper("com.test")
        )

        when:
        def members = assembler.getMemberVariables(
                ClientDetails.builder()
                        .requiresFetcher(requiresFetcher)
                        .requiresSubscriber(requiresSubscriber)
                        .build()
        )

        then:
        members.size() == (requiresFetcher ? 1 : 0) + (requiresSubscriber ? 1 : 0)
        getMemberVariable("fetcher", members)
                .map { it -> it.type.toString() == "${Fetcher.class.getName()}<com.test.Query, com.test.Mutation, Error>" }
                .orElse(true)
        getMemberVariable("subscriber", members)
                .map { it -> it.type.toString() == "${Subscriber.class.getName()}<com.test.Subscription, Error>" }
                .orElse(true)

        where:
        requiresFetcher | requiresSubscriber
        true            | true
        true            | false
        false           | true
        false           | false
    }

    static Optional<MemberVariable> getMemberVariable(String name, List<MemberVariable> variables) {
        Optional.ofNullable(variables.find { it.name == name })
    }

}
