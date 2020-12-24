package co.uk.jacobmountain.resolvers.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("PageInfo")
public class PageInfo {
    private String startCursor;

    private String endCursor;

    private Boolean hasNextPage;

    public String getStartCursor() {
        return this.startCursor;
    }

    public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
    }

    public String getEndCursor() {
        return this.endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public Boolean getHasNextPage() {
        return this.hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public String toString() {
        return "{ PageInfo startCursor: " + this.startCursor + ", endCursor: " + this.endCursor + ", hasNextPage: " + this.hasNextPage + " }";
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        PageInfo pageInfo = (PageInfo) other;
        return Objects.equals(this.startCursor, pageInfo.getStartCursor()) &&
                Objects.equals(this.endCursor, pageInfo.getEndCursor()) &&
                Objects.equals(this.hasNextPage, pageInfo.getHasNextPage());
    }
}
