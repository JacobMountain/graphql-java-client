package com.jacobmountain.resolvers.dto;

import lombok.Data;

@Data
public class PageInfo {

    private String startCursor;

    private String endCursor;

    private Boolean hasNextPage;

}
