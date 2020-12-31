package com.jacobmountain.graphql.client.exceptions;

import java.util.function.Supplier;

public class FieldNotFoundException extends RuntimeException {

    public FieldNotFoundException(String name) {
        super("Field of name \"" + name + "\" not found in schema");
    }

    public static Supplier<FieldNotFoundException> create(String field) {
        return () -> new FieldNotFoundException(field);
    }

}
