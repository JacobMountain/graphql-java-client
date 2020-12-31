package com.jacobmountain.graphql.client.exceptions;

public class QueryTypeNotFoundException extends RuntimeException {

    public QueryTypeNotFoundException() {
        super("Query type not found in schema");
    }

}
