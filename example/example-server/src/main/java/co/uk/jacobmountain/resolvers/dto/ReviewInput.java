package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("ReviewInput")
public class ReviewInput {
    private Integer stars;

    private String commentary;

    private ColorInput favorite_color;

    public Integer getStars() {
        return this.stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

    public String getCommentary() {
        return this.commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public ColorInput getFavorite_color() {
        return this.favorite_color;
    }

    public void setFavorite_color(ColorInput favorite_color) {
        this.favorite_color = favorite_color;
    }

    public String toString() {
        return "{ ReviewInput stars: " + this.stars + ", commentary: " + this.commentary + ", favorite_color: " + this.favorite_color + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ReviewInput reviewInput = (ReviewInput) other;
        return Objects.equals(this.stars, reviewInput.getStars()) &&
                Objects.equals(this.commentary, reviewInput.getCommentary()) &&
                Objects.equals(this.favorite_color, reviewInput.getFavorite_color());
    }
}
