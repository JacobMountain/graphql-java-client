package com.jacobmountain;


import com.jacobmountain.dto.Droid;
import com.jacobmountain.dto.Human;
import com.jacobmountain.graphql.client.annotations.GraphQLClient;
import com.jacobmountain.graphql.client.annotations.GraphQLQuery;

import java.util.List;

@GraphQLClient(
        schema = "src/test/resources/Schema.gql",
        mapping = {
                @GraphQLClient.Scalar(from = "ID", to = Integer.class)
        }
)
public interface Client {

        @GraphQLQuery("hero")
        com.jacobmountain.dto.Character getHero();

        @GraphQLQuery("droid")
        Droid getDroid(Integer id);

        @GraphQLQuery("humans")
        List<Human> getHumans();

}
