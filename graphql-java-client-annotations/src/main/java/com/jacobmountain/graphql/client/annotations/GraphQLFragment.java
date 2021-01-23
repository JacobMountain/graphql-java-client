package com.jacobmountain.graphql.client.annotations;

import java.lang.annotation.Repeatable;

@Repeatable(GraphQLFragments.class)
public @interface GraphQLFragment {

    String type();

    GraphQLField[] select() default {};

}
