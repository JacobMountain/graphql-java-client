package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.TypeMapper
import com.jacobmountain.graphql.client.dto.Response
import com.jacobmountain.graphql.client.modules.ClientDetails
import com.jacobmountain.graphql.client.modules.OptionalReturnStage
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.MethodDetails
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import spock.lang.Specification

import static com.jacobmountain.client.modules.CodeBlockUtils.renderBlocks

class OptionalReturnStageSpec extends Specification {

    TypeMapper typeMapper = new TypeMapper("com.test")

    OptionalReturnStage stage = new OptionalReturnStage(new Schema("""
            schema {
                query: Query
            }
            type Query {
                query: String
            }
            type Mutation {
                mutation: String
            }
        """), typeMapper)

    def "void return types don't generate any code"() {
        given: "a void return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.VOID)
                .mutation(true)
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then: "there shouldn't be any "
        blocks.isEmpty()
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

    def "Optional return types are mapped"() {
        given: "an optional return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ParameterizedTypeName.get(ClassName.get(Optional.class), ClassName.get(String.class)))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks) == "return ${Optional.class.name}.ofNullable(thing).map(${Response.class.name}::getData).map(com.test.Query::getField);"
    }

    def "non Optional return types are unwrapped"() {
        given: "a non Optional return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.get(String.class))
                .field("field")
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then:
        renderBlocks(blocks).endsWith(".map(com.test.Query::getField).orElse(null);")
    }
}
