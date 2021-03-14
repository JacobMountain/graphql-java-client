package com.jacobmountain.resolvers.dto;

import io.leangen.graphql.annotations.GraphQLIgnore;
import lombok.Data;

@Data
public class Review {

    @GraphQLIgnore
    private int id;

    private Episode episode;

    private Integer stars;

    private String commentary;

}
