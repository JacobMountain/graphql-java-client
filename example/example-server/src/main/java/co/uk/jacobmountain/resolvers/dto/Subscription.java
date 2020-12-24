package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("Subscription")
public class Subscription {
    private Review reviewAdded;

    public Review getReviewAdded() {
        return this.reviewAdded;
    }

    public void setReviewAdded(Review reviewAdded) {
        this.reviewAdded = reviewAdded;
    }

    public String toString() {
        return "{ Subscription reviewAdded: " + this.reviewAdded + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Subscription subscription = (Subscription) other;
        return Objects.equals(this.reviewAdded, subscription.getReviewAdded());
    }
}
