package com.jacobmountain.graphql.client.annotations;

public @interface GraphQLFragment {

    String type();

    GraphQLField[] select() default {};

}
