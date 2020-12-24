package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("Review")
public class Review {
    private Episode episode;

    private Integer stars;

    private String commentary;

    public Episode getEpisode() {
        return this.episode;
    }

    public void setEpisode(Episode episode) {
        this.episode = episode;
    }

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

    public String toString() {
        return "{ Review episode: " + this.episode + ", stars: " + this.stars + ", commentary: " + this.commentary + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Review review = (Review) other;
        return Objects.equals(this.episode, review.getEpisode()) &&
                Objects.equals(this.stars, review.getStars()) &&
                Objects.equals(this.commentary, review.getCommentary());
    }
}
