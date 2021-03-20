package com.jacobmountain.graphql.client.code;

import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MemberVariable {

    String name;

    TypeName type;

}
