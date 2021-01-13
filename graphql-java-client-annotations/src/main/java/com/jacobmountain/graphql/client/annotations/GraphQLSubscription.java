package com.jacobmountain.graphql.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GraphQLSubscription {

    /**
     * The query field to generate the query on
     */
    String value();

    /**
     * The name of the request
     */
    String name() default "";

    int maxDepth() default 5;

}
