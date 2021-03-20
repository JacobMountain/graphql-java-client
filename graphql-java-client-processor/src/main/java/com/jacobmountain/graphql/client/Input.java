package com.jacobmountain.graphql.client;

import com.jacobmountain.graphql.client.annotations.GraphQLClient;
import com.jacobmountain.graphql.client.exceptions.SchemaNotFoundException;
import com.jacobmountain.graphql.client.utils.Schema;
import com.jacobmountain.graphql.client.utils.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.TypeElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
@Value
public class Input {

    TypeElement element;

    @Getter(value = AccessLevel.PRIVATE)
    String packageStr;

    Path root;

    public Input(TypeElement element, Path root, String packageStr) {
        this.element = element;
        this.packageStr = packageStr;
        this.root = root;
    }

    public GraphQLClient getAnnotation() {
        return element.getAnnotation(GraphQLClient.class);
    }

    public TypeMapper getTypeMapper() {
        return new TypeMapper(getDtoPackage(), getAnnotation().mapping());
    }

    public String getDtoPackage() {
        return String.join(".", Arrays.asList(
                getPackage(),
                getAnnotation().dtoPackage()
        ));
    }

    public String getPackage() {
        return packageStr;
    }

    public Schema getSchema() {
        String value = getAnnotation().schema();
        File file = getSchemaFile();
        try {
            if (StringUtils.hasLength(value)) {
                log.info("Reading schema {}", file);
                return new Schema(getSchemaFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new SchemaNotFoundException(file.getPath());
    }

    public File getSchemaFile() {
        return root.resolve(getAnnotation().schema())
                .toAbsolutePath()
                .toFile();
    }

    public boolean isReactive() {
        return getAnnotation().reactive();
    }

}
