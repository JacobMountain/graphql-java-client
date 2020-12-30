package com.jacobmountain.exceptions;

public class QueryTypeNotFoundException extends RuntimeException {

    public QueryTypeNotFoundException() {
        super("Query type not found in schema");
    }

}
