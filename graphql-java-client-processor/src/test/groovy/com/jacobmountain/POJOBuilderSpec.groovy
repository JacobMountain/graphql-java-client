package com.jacobmountain

import com.jacobmountain.graphql.client.PojoBuilder
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import spock.lang.Specification

import javax.lang.model.element.Modifier

import static com.squareup.javapoet.TypeName.INT
import static com.squareup.javapoet.TypeName.VOID

class POJOBuilderSpec extends Specification {

    static boolean hasMethod(TypeSpec spec, String name) {
        spec.methodSpecs.find { method -> method.name == name } != null
    }

    static boolean methodMatchesType(TypeSpec spec, String name, TypeName returnType, ParameterSpec... params = []) {
        def method = spec.methodSpecs.find { method -> method.name == name }
        method.name == name &&
                method.returnType == returnType &&
                method.parameters == Arrays.asList(params) &&
                method.modifiers.contains(Modifier.PUBLIC)
    }

    def "We can generate a class"(){
        when:
        def build = PojoBuilder.newType("MyPojo", "com.jacobmountain").build()

        then:
        build.typeSpec.name == "MyPojo"
        build.typeSpec.kind == TypeSpec.Kind.CLASS
        hasMethod(build.typeSpec, "toString")
    }

    def "We can generate an interface"(){
        when:
        def build = PojoBuilder.newInterface("MyInterface", "com.jacobmountain").build()

        then:
        build.typeSpec.name == "MyInterface"
        build.typeSpec.kind == TypeSpec.Kind.INTERFACE
        !hasMethod(build.typeSpec, "toString")
    }

    def "We can generate getters and setters for private fields"(){
        when:
        def build = PojoBuilder.newType("MyPojo", "com.jacobmountain")
                .withField(ClassName.get(String.class), "field")
                .build()

        then:
        hasMethod(build.typeSpec, "setField")
        methodMatchesType(build.typeSpec, "setField", VOID, ParameterSpec.builder(ClassName.get(String.class), "set").build())
        hasMethod(build.typeSpec, "getField")
        methodMatchesType(build.typeSpec, "getField", ClassName.get(String.class))
    }

    def "We can generate getters and setters for multiple private fields"(){
        when:
        def build = PojoBuilder.newType("MyPojo", "com.jacobmountain")
                .withField(ClassName.get(String.class), "string")
                .withField(INT, "integer")
                .build()

        then:
        methodMatchesType(build.typeSpec, "setString", VOID, ParameterSpec.builder(ClassName.get(String.class), "set").build())
        methodMatchesType(build.typeSpec, "getString", ClassName.get(String.class))

        methodMatchesType(build.typeSpec, "setInteger", VOID, ParameterSpec.builder(INT, "set").build())
        methodMatchesType(build.typeSpec, "getInteger", INT)
    }

    def "My Class can implement an interface"() {
        given:
        PojoBuilder.newInterface("Interface", "com.jacobmountain").build()

        when:
        def clazz = PojoBuilder.newType("Implements", "com.jacobmountain")
                .implement("Interface")
                .build()

        then:
        clazz.typeSpec.superinterfaces.contains(ClassName.get("com.jacobmountain", "Interface"))
    }

}
