package co.uk.jacobmountain;

import com.squareup.javapoet.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
public class PojoBuilder {

    private TypeSpec.Builder builder;

    private boolean isInterface;

    private final List<String> fields = new ArrayList<>();

    private final List<String> subTypes = new ArrayList<>();

    private final String name;

    private final String packageName;

    public static PojoBuilder newInterface(String name, String packageName) {
        return new PojoBuilder(name, packageName).interfac(name);
    }

    public static PojoBuilder newClass(String name, String packageName) {
        return new PojoBuilder(name, packageName).clazz(name);
    }

    private PojoBuilder(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;;
    }

    private PojoBuilder clazz(String name) {
        log.info(String.format("type %s {", name));
        isInterface = false;
        builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    private PojoBuilder interfac(String name) {
        log.info(String.format("interface %s {", name));
        isInterface = true;
        builder = TypeSpec.interfaceBuilder(name).addModifiers(Modifier.PUBLIC);
        return this;
    }

    public PojoBuilder withField(TypeName clazz, String name) {
        if (clazz instanceof ClassName) {
            log.info("\t" + name + ": " + ((ClassName) clazz).simpleName());
        } else {
            log.info("\t" + name + ": " + clazz);
        }
        if (!isInterface) {
            fields.add(name);
            builder.addField(clazz, name, Modifier.PRIVATE);
        }
        withAccessors(clazz, name);
        return this;
    }

    public PojoBuilder withSubType(String type){
        this.subTypes.add(type);
        return this;
    }

    public PojoBuilder implement(String s) {
        builder.addSuperinterface(ClassName.get(packageName, s));
        return this;
    }

    private void withAccessors(TypeName clazz, String name) {
        withGetter(clazz, name);
        if (!isInterface) {
            withSetter(clazz, name);
        }
    }

    private MethodSpec.Builder accessorBuilder(String name, String body, Object... args) {
        MethodSpec.Builder getter = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC);
        if (!isInterface) {
            getter.addStatement(body, args);
        } else {
            getter.addModifiers(Modifier.ABSTRACT);
        }
        return getter;
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

    private void generateToString() {
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
                builder.append("\", ");
            }
        }
        builder.append("\"}\"");
        MethodSpec toString = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return $L", builder)
                .build();
        this.builder.addMethod(toString);
    }

    private void generateEquals() {
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

    public void build(Filer filer) throws IOException {
        build().writeTo(filer);
    }

    public JavaFile build() {
        log.info("}");
        if (!isInterface) {
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

}
