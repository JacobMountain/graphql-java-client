package com.jacobmountain.resolvers.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendsConnection {

	private Integer totalCount;

	private List<FriendsEdge> edges;

	private List<Character> friends;

	private PageInfo pageInfo;

}
