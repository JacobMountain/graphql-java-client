package co.uk.jacobmountain.exceptions;

public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException() {
        super("Failed to find the graphql schema file");
    }

}
