//package com.jacobmountain.client.code
//
//import com.jacobmountain.graphql.client.TypeMapper
//import com.jacobmountain.graphql.client.code.ClientGenerator
//import com.jacobmountain.graphql.client.utils.Schema
//import spock.lang.Specification
//
//import javax.annotation.processing.Filer
//import javax.lang.model.element.AnnotationMirror
//import javax.lang.model.element.Element
//import javax.lang.model.element.ElementKind
//import javax.lang.model.element.ElementVisitor
//import javax.lang.model.element.Modifier
//import javax.lang.model.element.Name
//import javax.lang.model.element.NestingKind
//import javax.lang.model.element.TypeElement
//import javax.lang.model.element.TypeParameterElement
//import javax.lang.model.type.TypeMirror
//import java.lang.annotation.Annotation
//
//class ClientGeneratorSpec extends Specification {
//
//    ClientGenerator generator = new ClientGenerator(
//            Mock(Filer),
//            new TypeMapper("com.test"),
//            "com.test",
//            new Schema("""
//            schema {
//                query: Query
//            }
//            type Query {}
//            """),
//            false
//    )
//
//    def "Test"() {
//        given:
//        Element el = Mock(TypeElement) {
//            getSimpleName() >> Mock(Name) {
//                toString() >> "MyClient"
//            }
//            getEnclosingElement() >> new TypeElement() {
//                @Override
//                List<? extends Element> getEnclosedElements() {
//                    return null
//                }
//
//                @Override
//                NestingKind getNestingKind() {
//                    return null
//                }
//
//                @Override
//                Name getQualifiedName() {
//                    return null
//                }
//
//                @Override
//                Name getSimpleName() {
//                    return null
//                }
//
//                @Override
//                TypeMirror getSuperclass() {
//                    return null
//                }
//
//                @Override
//                List<? extends TypeMirror> getInterfaces() {
//                    return null
//                }
//
//                @Override
//                List<? extends TypeParameterElement> getTypeParameters() {
//                    return null
//                }
//
//                @Override
//                Element getEnclosingElement() {
//                    return null
//                }
//
//                @Override
//                TypeMirror asType() {
//                    return null
//                }
//
//                @Override
//                ElementKind getKind() {
//                    return null
//                }
//
//                @Override
//                Set<Modifier> getModifiers() {
//                    return null
//                }
//
//                @Override
//                List<? extends AnnotationMirror> getAnnotationMirrors() {
//                    return null
//                }
//
//                @Override
//                def <A extends Annotation> A getAnnotation(Class<A> annotationType) {
//                    return null
//                }
//
//                @Override
//                def <R, P> R accept(ElementVisitor<R, P> v, P p) {
//                    return null
//                }
//
//                @Override
//                def <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
//                    return null
//                }
//            }
//        }
//
//        when:
//        generator.generate(el, "impl")
//
//        then:
//        true
//    }
//
//}
