package com.jacobmountain.graphql.client.annotations;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Repeatable(GraphQLFragments.class)
public @interface GraphQLFragment {

    String name() default "";

    String type();

    GraphQLField[] select() default {};

}
