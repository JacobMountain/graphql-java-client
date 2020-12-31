package com.jacobmountain.graphql.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request<A> {

    private String query;

    private A variables;

}

