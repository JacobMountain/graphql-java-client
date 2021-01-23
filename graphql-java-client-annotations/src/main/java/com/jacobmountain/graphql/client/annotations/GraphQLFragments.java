package com.jacobmountain.graphql.client.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface GraphQLFragments {

    GraphQLFragment[] value();

}
