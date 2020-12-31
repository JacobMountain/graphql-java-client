package com.jacobmountain.graphql.client.exceptions;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String schema) {
        super("Failed to find the graphql schema file: " + schema);
    }

}
