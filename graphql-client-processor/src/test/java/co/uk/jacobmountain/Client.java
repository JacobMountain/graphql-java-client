package co.uk.jacobmountain;


import co.uk.jacobmountain.dto.*;

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

}
