package co.uk.jacobmountain;


import co.uk.jacobmountain.dto.Droid;

import java.util.List;

@GraphQLClient(
        schema = "src/test/resources/Schema.gql",
        mapping = {
                @GraphQLClient.Scalar(from = "ID", to = Integer.class)
        }
)
public interface Client {

        @GraphQLQuery("hero")
        co.uk.jacobmountain.dto.Character getHero();

        @GraphQLQuery("droid")
        Droid getDroid(Integer id);

        @GraphQLQuery("humans")
        List<co.uk.jacobmountain.dto.Human> getHumans();

}
