package com.jacobmountain.resolvers.dto;

import io.leangen.graphql.annotations.types.GraphQLInterface;

import java.util.List;


@GraphQLInterface(name = "Character", implementationAutoDiscovery = true)
public interface Character {

    String getId();

    String getName();

    List<String> getFriends();

    List<Episode> getAppearsIn();

}
