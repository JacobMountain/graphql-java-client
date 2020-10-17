package co.uk.jacobmountain

import co.uk.jacobmountain.dto.Character
import co.uk.jacobmountain.dto.Human
import spock.lang.Specification

class ProcessorSpec extends Specification {

    def "POJO generator generates type in schema"() {
        when:
        def hero = new Human()
        hero.name = "Jacob"

        println hero

        then:
        hero.name == "Jacob"
        hero.toString() == "{ Human id: null, name: Jacob, friends: null, totalCredits: 0 }"
    }

    def "Types can extend interfaces"() {
        when:
        def hero = new Human()
        hero.name = "Jacob"

        println hero

        then:
        hero instanceof Character
        hero.name == "Jacob"
        hero.toString() == "{ Human id: null, name: Jacob, friends: null, totalCredits: 0 }"
    }

    def "Types can be mapped"(){
        when:
        def hero = new Human()
        hero.id = 1234

        println hero

        then:
        hero.id == 1234
        hero.toString() == "{ Human id: 1234, name: null, friends: null, totalCredits: 0 }"
    }

    def "Lists can be mapped"() {
        when:
        def hero = newHuman("Fred")
        hero.friends = [
                newHuman("Jacob")
        ]

        println hero

        then:
        hero.friends.size() == 1
        hero.friends[0].getName() == "Jacob"
        hero.toString() == "{ Human id: 1, name: Fred, friends: [{ Human id: 2, name: Jacob, friends: null, totalCredits: 0 }], totalCredits: 0 }"
    }

    def "Build a equals"() {
        given:
        def a = newHuman("Fred")

        def b = newHuman("John")

        expect:
        a != b
    }

    def "Build a equals 2"() {
        given:
        def a = newHuman("Fred")

        def b = new Human()
        b.id = a.id
        b.name = "Fred"

        expect:
        a == b
    }

    static int id = 1

    static Human newHuman(String name) {
        def human = new Human()
        human.id = id++
        human.name = name
        return human
    }


}
