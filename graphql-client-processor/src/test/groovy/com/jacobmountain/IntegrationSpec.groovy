package com.jacobmountain

import com.jacobmountain.dto.*
import com.jacobmountain.graphql.client.Fetcher
import com.jacobmountain.graphql.client.dto.Response
import spock.lang.Specification
import spock.lang.Unroll

class IntegrationSpec extends Specification {

    Fetcher<Query, Void, Void> fetcher = Mock(Fetcher)

    Client graph = new ClientGraph(fetcher)

    static def newCharacter(int id, String name) {
        def character = new Human()
        character.id = id
        character.name = name
        character
    }

    static def newDroid(int id, String name) {
        def character = new Droid()
        character.id = id
        character.name = name
        character
    }

    def "Can query for data with no args"() {
        given:
        fetcher.query(_, null) >> Response.builder()
                .data(new Query() {
                    @Override
                    Character getHero() {
                        return newCharacter(1, "Obi Wan")
                    }
                })
                .build()

        when:
        def hero = graph.getHero()

        then:
        hero.id == 1
        hero.name == "Obi Wan"
    }

    @Unroll
    def "Can query for data with args (#name)"() {
        given:
        def args = new GetDroidById()
        args.id = id
        def droid = newDroid(id, name)
        fetcher.query(_, args) >> Response.builder()
                .data(new Query() {
                    @Override
                    Droid getDroid() {
                        return droid
                    }
                })
                .build()

        when:
        def hero = graph.getDroid(id)

        then:
        hero.id == id
        hero.name == name

        where:
        id | name
        2  | "R2-D2"
        3  | "C3P0"
    }

}
