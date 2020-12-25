package co.uk.jacobmountain;

import co.uk.jacobmountain.utils.AnnotationUtils;
import co.uk.jacobmountain.utils.StringUtils;
import com.squareup.javapoet.*;
import graphql.language.EnumValueDefinition;
import lombok.extern.slf4j.Slf4j;

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

    public static PojoBuilder newInterface(String name, String packageName) {
        return new PojoBuilder(name, packageName).interfac(name);
    }

    public static PojoBuilder newClass(String name, String packageName) {
        return new PojoBuilder(name, packageName).clazz(name);
    }

    public static PojoBuilder newEnum(String name, String packageName) {
        return new PojoBuilder(name, packageName).enumeration(name);
    }

    private PojoBuilder clazz(String name) {
        log.info(String.format("type %s {", name));
        type = Type.Class;
        builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    private PojoBuilder interfac(String name) {
        log.info(String.format("interface %s {", name));
        type = Type.Interface;
        builder = TypeSpec.interfaceBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    private PojoBuilder enumeration(String name) {
        type = Type.Enum;
        builder = TypeSpec.enumBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    public PojoBuilder withField(TypeName clazz, String name) {
        if (clazz instanceof ClassName) {
            log.info("\t" + name + ": " + ((ClassName) clazz).simpleName());
        } else {
            log.info("\t" + name + ": " + clazz);
        }
        fields.add(name);
        if (!isInterface()) {
            builder.addField(clazz, name, Modifier.PRIVATE);
        }
        withAccessors(clazz, name);
        return this;
    }

    public void withEnumValue(EnumValueDefinition it) {
        if (type == Type.Enum) {
            builder.addEnumConstant(it.getName());
        }
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

    private void withGetter(TypeName clazz, String name) {
        builder.addMethod(
                accessorBuilder(StringUtils.camelCase("get", name), "return this.$L", name)
                        .returns(clazz)
                        .build()
        );
    }

    private void withSetter(TypeName clazz, String name) {
        builder.addMethod(
                accessorBuilder(StringUtils.camelCase("set", name), "this.$L = $L", name, name)
                        .addParameter(clazz, name)
                        .returns(void.class)
                        .build()
        );
    }

    private void generateEquals() {
        if (type == Type.Enum) {
            return;
        }
        String variable = StringUtils.camelCase(name);
        CodeBlock.Builder equals = CodeBlock.builder();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            equals.add(CodeBlock.of("$T.equals(this.$L, $L.$L())", Objects.class, field, variable, StringUtils.camelCase("get", field)));
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
        return type == Type.Interface;
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
        if (type != Type.Enum) {
            log.info("}");
        } else {
            log.info(String.format("enum %s", name));
        }
    }

    enum Type {
        Class,
        Interface,
        Enum
    }

}
