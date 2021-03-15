package com.jacobmountain.client.modules.reactive

import com.jacobmountain.graphql.client.TypeMapper
import com.jacobmountain.graphql.client.code.reactive.ReactiveReturnStage
import com.jacobmountain.graphql.client.dto.Response
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.ClientDetails
import com.jacobmountain.graphql.client.visitor.MethodDetails
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.function.Function

import static com.jacobmountain.client.modules.CodeBlockUtils.renderBlocks

class ReactiveReturnStageSpec extends Specification {

    TypeMapper typeMapper = new TypeMapper("com.test")

    ReactiveReturnStage stage = new ReactiveReturnStage(new Schema("""
            schema {
                query: Query
            }
            type Query {

            }
        """), typeMapper)

    def "Publisher return types are mapped"() {
        given: "a mono return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Mono.class), ClassName.get(String.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks) == "return ${Mono.class.name}.from(thing).map(${Response.class.name}::getData).filter(data -> ${Objects.class.name}.nonNull(data.getField())).map(com.test.Query::getField);"
    }

    def "void return types are only supported on mutations"() {
        given: "a void return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.VOID)
                .mutation(false)
                .build()

        when:
        stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        thrown(IllegalArgumentException)
    }

    def "Optional return types are blocked"() {
        given: "an optional return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Optional.class), ClassName.get(String.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".blockOptional();")
    }

    def "Non publisher return types are blocked"() {
        given: "a mono return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.get(String.class))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".block();")
    }

    def "Fluxs are flatmapped"() {
        given: "a flux return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Flux.class), ClassName.get(String.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".flatMapIterable(${Function.class.name}.identity());")
    }

    def "void return types are blocked"() {
        given: "a void return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.VOID)
                .mutation(true)
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks) == "${Mono.class.name}.from(thing).block();"
    }

    def "Mono<Void> return types are blocked"() {
        given: "a void return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Mono.class), ClassName.get(Void.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".map(com.test.Query::getField).then();")
    }

}
