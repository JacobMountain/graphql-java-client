package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("Mutation")
public class Mutation {
    private Review createReview;

    public Review getCreateReview() {
        return this.createReview;
    }

    public void setCreateReview(Review createReview) {
        this.createReview = createReview;
    }

    public String toString() {
        return "{ Mutation createReview: " + this.createReview + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Mutation mutation = (Mutation) other;
        return Objects.equals(this.createReview, mutation.getCreateReview());
    }
}
