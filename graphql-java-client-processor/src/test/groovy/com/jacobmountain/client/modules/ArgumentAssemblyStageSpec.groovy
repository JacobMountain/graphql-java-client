package com.jacobmountain.client.modules

import com.jacobmountain.graphql.client.modules.ArgumentAssemblyStage
import com.jacobmountain.graphql.client.modules.ClientDetails
import com.jacobmountain.graphql.client.visitor.MethodDetails
import com.jacobmountain.graphql.client.visitor.Parameter
import spock.lang.Specification

import static com.jacobmountain.client.modules.CodeBlockUtils.renderBlocks

class ArgumentAssemblyStageSpec extends Specification {

    ArgumentAssemblyStage stage = new ArgumentAssemblyStage()

    def "When there's no paramaters, we should return"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .parameters([])
                .build()
        )
        def code = renderBlocks(blocks).split(";")

        then:
        renderBlocks(blocks).split(";") == []
    }

    def "When there's a parameter, we should set it"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .methodName("getQuery")
                .parameter(Parameter.builder().name("arg1").build())
                .parameter(Parameter.builder().name("arg2").build())
                .build()
        )
        def code = renderBlocks(blocks).split(";")

        then:
        code[0] == "GetQueryByArg1AndArg2 args = new GetQueryByArg1AndArg2()"
        code[1].contains("args.setArg1(")
        code[2].contains("args.setArg2(")
    }

    def "When there's a non-nullable parameter, we should check it client side"() {
        when:
        def blocks = stage.assemble(Mock(ClientDetails), MethodDetails.builder()
                .methodName("getQuery")
                .parameter(Parameter.builder()
                        .name("nonNull")
                        .nullable(false)
                        .build()
                )
                .build()
        )
        def code = renderBlocks(blocks).split(";")

        then:
        code[1] == """args.setNonNull(java.util.Objects.requireNonNull(nonNull, "nonNull is not nullable"))"""
    }

}
