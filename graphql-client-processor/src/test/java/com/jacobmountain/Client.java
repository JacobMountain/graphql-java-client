package com.jacobmountain;


import com.jacobmountain.dto.Character;
import com.jacobmountain.dto.Droid;
import com.jacobmountain.dto.Human;

import java.util.List;

@GraphQLClient(
        schema = "src/test/resources/Schema.gql",
        mapping = {
                @GraphQLClient.Scalar(from = "ID", to = Integer.class)
        }
)
public interface Client {

        @GraphQLQuery("hero")
        Character getHero();

        @GraphQLQuery("droid")
        Droid getDroid(Integer id);

        @GraphQLQuery("humans")
        List<Human> getHumans();

}
