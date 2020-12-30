package com.jacobmountain.resolvers.dto;

import lombok.Data;

@Data
public class Review {

    private Episode episode;

    private Integer stars;

    private String commentary;

}
