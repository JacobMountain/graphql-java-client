package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Objects;

@JsonTypeName("Query")
public class Query {
    private Character hero;

    private List<Review> reviews;

    public Character getHero() {
        return this.hero;
    }

    public void setHero(Character hero) {
        this.hero = hero;
    }

    public List<Review> getReviews() {
        return this.reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String toString() {
        return "{ Query hero: " + this.hero + ", reviews: " + this.reviews + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Query query = (Query) other;
        return Objects.equals(this.hero, query.getHero()) &&
                Objects.equals(this.reviews, query.getReviews());
    }
}
