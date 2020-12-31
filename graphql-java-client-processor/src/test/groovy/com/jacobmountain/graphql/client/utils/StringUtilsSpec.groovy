package com.jacobmountain.graphql.client.utils

import spock.lang.Specification

class StringUtilsSpec extends Specification {

    def "Capitalize"(){
        when:
        def result = StringUtils.capitalize(word)

        then:
        result == expected

        where:
        word        | expected
        "camel"     | "Camel"
        "oneword"   | "Oneword"
        "two words" | "Two words"
    }

    def "Decapitalize"(){
        when:
        def result = StringUtils.decapitalize(word)

        then:
        result == expected

        where:
        word        | expected
        "Camel"     | "camel"
        "Oneword"   | "oneword"
        "two words" | "two words"
    }

    def "Camel case"(){
        when:
        def result = StringUtils.camelCase(word.split(" "))

        then:
        result == expected

        where:
        word          | expected
        "Camel case"  | "camelCase"
        "Oneword"     | "oneword"
        "two words"   | "twoWords"
    }

    def "Encase in quotes"(){
        when:
        def result = StringUtils.enquote(word)

        then:
        result == expected

        where:
        word          | expected
        "Camel case"  | '"Camel case"'
        "Oneword"     | '"Oneword"'
        "two words"   | '"two words"'
    }

    def "is empty"(){
        when:
        def result = StringUtils.isEmpty(word)

        then:
        result == isEmpty

        where:
        word        | isEmpty
        null        | true
        ""          | true
        " "         | true
        "word"      | false
        " word "    | false
        " word    " | false
    }


}
