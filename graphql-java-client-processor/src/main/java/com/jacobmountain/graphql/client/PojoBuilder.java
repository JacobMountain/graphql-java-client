package com.jacobmountain.graphql.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jacobmountain.graphql.client.utils.AnnotationUtils;
import com.jacobmountain.graphql.client.utils.StringUtils;
import com.squareup.javapoet.*;
import graphql.language.EnumValueDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
public class PojoBuilder {

    private TypeSpec.Builder builder;

    private Type type;

    private final String name;

    private final String packageName;

    private final List<String> fields = new ArrayList<>();

    private final List<String> subTypes = new ArrayList<>();

    protected PojoBuilder(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public String getFQDN() {
        return String.format("%s.%s", packageName, name);
    }

    public static PojoBuilder newInterface(String name, String packageName) {
        return new PojoBuilder(name, packageName).interfac(name);
    }

    public static PojoBuilder newType(String name, String packageName) {
        return new PojoBuilder(name, packageName).clazz(name, false);
    }

    public static PojoBuilder newInput(String name, String packageName) {
        return new PojoBuilder(name, packageName).clazz(name, true);
    }

    public static PojoBuilder newEnum(String name, String packageName) {
        return new PojoBuilder(name, packageName).enumeration(name);
    }

    public static PojoBuilder newUnion(String name, String packageName) {
        return new PojoBuilder(name, packageName).union(name);
    }

    private PojoBuilder clazz(String name, boolean input) {
        log.info("{} {} {", input ? "input" : "type", name);
        type = Type.Class;
        builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationUtils.generated());
        return this;
    }

    private PojoBuilder interfac(String name) {
        log.info("interface {} {", name);
        type = Type.Interface;
        builder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationUtils.generated());
        return this;
    }

    private PojoBuilder union(String name) {
        type = Type.Union;
        builder = TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationUtils.generated());
        return this;
    }

    private PojoBuilder enumeration(String name) {
        log.info("enum {} {", name);
        type = Type.Enum;
        builder = TypeSpec.enumBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    public PojoBuilder withField(TypeName clazz, String name) {
        boolean keyword = SourceVersion.isKeyword(name);
        String finalName = name;
        if (keyword) {
            finalName = "_" + name;
        }
        if (clazz instanceof ClassName) {
            log.info("\t" + name + ": " + ((ClassName) clazz).simpleName());
        } else {
            log.info("\t" + name + ": " + clazz);
        }
        fields.add(finalName);
        if (!isInterface()) {
            builder.addField(
                    FieldSpec.builder(clazz, finalName, Modifier.PRIVATE)
                            .addAnnotation(
                                    AnnotationSpec.builder(JsonProperty.class)
                                            .addMember("value", "\"$L\"", name)
                                            .build()
                            ).build()
            );
        }
        withAccessors(clazz, finalName);
        return this;
    }

    public void withEnumValue(EnumValueDefinition it) {
        if (type == Type.Enum) {
            builder.addEnumConstant(it.getName());
            log.info("\t{}", it.getName());
        }
    }

    public boolean hasSubType(String type) {
        return this.subTypes.contains(type);
    }

    public PojoBuilder withSubType(String type) {
        this.subTypes.add(type);
        return this;
    }

    private void withAccessors(TypeName clazz, String name) {
        withGetter(clazz, name);
        if (!isInterface()) {
            withSetter(clazz, name);
        }
    }

    public PojoBuilder implement(String s) {
        builder.addSuperinterface(ClassName.get(packageName, s));
        return this;
    }

    private MethodSpec.Builder accessorBuilder(String name, String body, Object... args) {
        MethodSpec.Builder getter = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC);
        if (!isInterface()) {
            getter.addStatement(body, args);
        } else {
            getter.addModifiers(Modifier.ABSTRACT);
        }
        return getter;
    }

    private void generateToString() {
        if (type == Type.Enum) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\"{ ");
        builder.append(this.name);
        for (String field : fields) {
            builder.append(" ");
            builder.append(field);
            builder.append(": \" + ");
            builder.append("this.");
            builder.append(field);
            builder.append(" + ");
            if (!field.equals(fields.get(fields.size() - 1))) {
                builder.append("\",");
            }
        }
        builder.append("\" }\"");
        MethodSpec toString = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $L", builder)
                .build();
        this.builder.addMethod(toString);
    }

    private String createGetterName(String variable) {
        return StringUtils.camelCase("get",
                variable.replaceFirst("_", "")
        );
    }

    private void withGetter(TypeName clazz, String name) {
        String methodName = createGetterName(name);
        builder.addMethod(
                accessorBuilder(methodName, "return this.$L", name)
                        .returns(clazz)
                        .build()
        );
    }

    private String createSetterName(String variable) {
        return StringUtils.camelCase(
                "set",
                variable.replaceFirst("_", "")
        );
    }

    private void withSetter(TypeName clazz, String name) {
        String methodName = createSetterName(name);
        builder.addMethod(
                accessorBuilder(methodName, "this.$L = $L", name, "set")
                        .addParameter(clazz, "set")
                        .returns(void.class)
                        .build()
        );
    }

    private void generateEquals() {
        if (type == Type.Enum) {
            return;
        }
        String variable = StringUtils.camelCase("other", name);
        CodeBlock.Builder equals = CodeBlock.builder();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            equals.add(CodeBlock.of("$T.equals(this.$L, $L.$L())", Objects.class, field, variable, createGetterName(field)));
            if (i + 1 != fields.size()) {
                equals.add(" &&\n\t");
            }
        }
        if (fields.isEmpty()) {
            equals = CodeBlock.builder().add("true");
        }
        this.builder.addMethod(
                MethodSpec.methodBuilder("equals")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(TypeName.OBJECT, "other")
                        .returns(TypeName.BOOLEAN)
                        .addStatement("if (this == other) return true")
                        .addStatement("if (other == null || getClass() != other.getClass()) return false")
                        .addStatement("$L $L = ($L) other", name, variable, name)
                        .addCode("return ")
                        .addCode(equals.build())
                        .addCode(";\n")
                        .build()
        );
    }

    boolean isInterface() {
        return type == Type.Interface || type == Type.Union;
    }

    public JavaFile build() {
        if (!isInterface()) {
            generateToString();
            generateEquals();
        } else {
            builder.addAnnotation(AnnotationUtils.JSON_TYPE_INFO);
            builder.addAnnotation(AnnotationUtils.jsonSubtypes(subTypes.toArray(new String[0])));
        }
        builder.addAnnotation(
                AnnotationSpec.builder(AnnotationUtils.JSON_TYPE_NAME_ANNOTATION)
                        .addMember("value", StringUtils.enquote(name))
                        .build()
        );
        return JavaFile.builder(packageName, builder.build())
                .indent("\t")
                .build();
    }

    public void finalise() {
        if (type == Type.Union) {
            log.info("union {} = {}", name, String.join(" | ", subTypes));
        } else {
            log.info("}");
        }
        log.info("");
    }

    enum Type {
        Class,
        Interface,
        Enum,
        Union
    }

}
