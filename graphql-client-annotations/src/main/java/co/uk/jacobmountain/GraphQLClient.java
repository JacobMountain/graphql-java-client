package co.uk.jacobmountain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GraphQLClient {

    String schema();

    Scalar[] mapping() default {};

    int maxDepth() default 3;

    boolean nullChecking() default false;

    @interface Scalar {

        String from();

        Class<?> to();

    }

}
