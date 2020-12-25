package co.uk.jacobmountain


import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import spock.lang.Specification
import spock.lang.Subject

class DTOGeneratorSpec extends Specification {

    FileWriter writer = Mock(FileWriter)

    TypeMapper mapper = new TypeMapper("com.package")

    @Subject
    DTOGenerator generator = new DTOGenerator("com.package", writer, mapper)

    static TypeDefinitionRegistry REGISTRY

    static {
        def resource = ResourceUtils.readResource("Schema.gql")
        REGISTRY = new SchemaParser().parse(resource)
    }

    void generateTypes(String... types) {
        generator.generate(types.collect { it -> REGISTRY.getType(it).orElse(null) })
    }

    def "We can create an object"() {
        given:
        PojoBuilder actual

        when:
        generateTypes("Thing")

        then:
        1 * writer.write({ arg -> arg.name == "Thing" }) >> { actual = it[0] }
        actual.name == "Thing"
        actual.packageName == "com.package"
        actual.fields.containsAll(["field", "number"])
        actual.isInterface() == false
        actual.type == PojoBuilder.Type.Class
    }

    def "We can create an object that implements an interface"() {
        given:
        PojoBuilder impl
        PojoBuilder interfac

        when:
        generateTypes("Character", "Human")

        then: "The POJOs should be built"
        1 * writer.write({ arg -> arg.name == "Character" }) >> { interfac = it[0] }
        1 * writer.write({ arg -> arg.name == "Human" }) >> { impl = it[0] }

        and: "The interface generated correctly"
        interfac.name == "Character"
        interfac.packageName == "com.package"
        interfac.fields.containsAll(["id", "name", "friends"])
        interfac.isInterface()
        interfac.type == PojoBuilder.Type.Interface

        and: "The implementation generated correctly"
        impl.name == "Human"
        impl.packageName == "com.package"
        impl.fields.containsAll(["id", "name", "friends", "totalCredits"])
        impl.isInterface() == false
        impl.type == PojoBuilder.Type.Class
    }

    def "Enums"() {
        given:
        PojoBuilder actual

        when:
        generateTypes("Numbers")

        then:
        1 * writer.write({ arg -> arg.name == "Numbers" }) >> { actual = it[0] }
        actual.name == "Numbers"
        actual.packageName == "com.package"
        actual.type == PojoBuilder.Type.Enum
    }

    def "Unions"() {
        given:
        PojoBuilder actual

        when:
        generateTypes("Character", "Human", "Droid", "Union")

        then:
        1 * writer.write({ arg -> arg.name == "Union" }) >> { actual = it[0] }
        actual.name == "Union"
        actual.packageName == "com.package"
        actual.type == PojoBuilder.Type.Interface
    }

    def "Input"() {
        given:
        PojoBuilder actual

        when:
        generateTypes("InputThing")

        then:
        1 * writer.write({ arg -> arg.name == "InputThing" }) >> { actual = it[0] }
        actual.name == "InputThing"
        actual.packageName == "com.package"
        actual.type == PojoBuilder.Type.Class
        actual.fields.containsAll(["number"])
    }

}
