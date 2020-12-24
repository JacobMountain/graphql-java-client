package co.uk.jacobmountain.resolvers.dto;

import io.leangen.graphql.annotations.GraphQLInputField;
import lombok.Data;

@Data
public class ReviewInput {

    private Integer stars;

    private String commentary;

    @GraphQLInputField(name = "favourite_color")
    private ColorInput favouriteColor;

}
