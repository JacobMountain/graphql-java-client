package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.TypeMapper
import com.jacobmountain.graphql.client.modules.ClientDetails
import com.jacobmountain.graphql.client.modules.OptionalReturnStage
import com.jacobmountain.graphql.client.utils.Schema
import com.jacobmountain.graphql.client.visitor.MethodDetails
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.ParameterizedTypeName
import spock.lang.Specification

class OptionalReturnStageSpec extends Specification {

    Schema schema = new Schema("""
            schema {
                query: Query
            }
            type Query {

            }
        """)

    TypeMapper typeMapper = new TypeMapper("com.jacobmountain")

    OptionalReturnStage stage = new OptionalReturnStage(schema, typeMapper)

    def "void return types don't generate any code"() {
        given: "a void return type"
        MethodDetails methodDetails = MethodDetails.builder()
                .returnType(ClassName.VOID)
                .build()

        when:
        def blocks = stage.assemble(Mock(ClientDetails), methodDetails)

        then: "there shouldn't be any "
        blocks.isEmpty()
    }

    static def renderBlocks(List<CodeBlock> blocks) {
        blocks.collect { block ->
            block.toString()
                    .replaceAll("\\t|\\n", "")
        }.join(";") + ";"
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
        renderBlocks(blocks) == "return java.util.Optional.ofNullable(thing).map(com.jacobmountain.graphql.client.dto.Response::getData).map(com.jacobmountain.Query::getField);"
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
        renderBlocks(blocks).endsWith(".map(com.jacobmountain.Query::getField).orElse(null);")
    }
}
