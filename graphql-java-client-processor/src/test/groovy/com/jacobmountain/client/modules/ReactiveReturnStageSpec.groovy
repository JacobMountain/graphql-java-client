package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.TypeMapper
import com.jacobmountain.graphql.client.dto.Response
import com.jacobmountain.graphql.client.modules.ClientDetails
import com.jacobmountain.graphql.client.modules.ReactiveReturnStage
import com.jacobmountain.graphql.client.utils.Schema
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
        renderBlocks(blocks) == "return ${Mono.class.name}.from(thing).map(${Response.class.name}::getData).map(com.test.Query::getField);"
    }

    def "Optional return types are blocked"() {
        given: "a mono return type"
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
        renderBlocks(blocks).endsWith(".blockOptional().orElse(null);")
    }

    def "Fluxs are flatmapped"() {
        given: "a mono return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Flux.class), ClassName.get(String.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".flatMapIterable(${Function.class.name}.identity());")
    }

}
