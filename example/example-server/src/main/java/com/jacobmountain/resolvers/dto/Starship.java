package com.jacobmountain.resolvers.dto;

import lombok.Data;

import java.util.List;

@Data
public class Starship implements SearchResult {

    private String id;

    private String name;

    private Float length;

    private List<Float> coordinates;

}
