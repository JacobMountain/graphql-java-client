package co.uk.jacobmountain;


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
        Droid getDroid(int id);

}
