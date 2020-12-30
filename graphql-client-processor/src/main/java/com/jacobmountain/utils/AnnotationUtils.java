package com.jacobmountain.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AnnotationUtils {


    public static final String JACKSON_ANNOTATIONS_PACKAGE = "com.fasterxml.jackson.annotation";

    public static final ClassName JSON_SUB_TYPES_ANNOTATION = ClassName.get(JACKSON_ANNOTATIONS_PACKAGE, "JsonSubTypes");
    public static final ClassName TYPE_ANNOTATION = ClassName.get(JACKSON_ANNOTATIONS_PACKAGE, "JsonSubTypes", "Type");
    public static final ClassName JSON_TYPE_NAME_ANNOTATION = ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeName");

    public static final AnnotationSpec JSON_TYPE_INFO = AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.annotation", "JsonTypeInfo"))
            .addMember("use", "JsonTypeInfo.Id.NAME")
            .addMember("include", "JsonTypeInfo.As.PROPERTY")
            .addMember("property", StringUtils.enquote("__typename"))
            .build();

    public AnnotationSpec jsonSubtypes(String... types) {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("{\n");

        for (int i = 0; i < types.length; i++) {
            builder.add("$L",
                    AnnotationSpec.builder(TYPE_ANNOTATION)
                            .addMember("value", types[i] + ".class")
                            .build()
            );
            if (i + 1 != types.length) {
                builder.add(",");
            }
            builder.add("\n");
        }
        return AnnotationSpec.builder(JSON_SUB_TYPES_ANNOTATION)
                .addMember("value", "$L",  builder.add("}").build())
                .build();
    }

}
