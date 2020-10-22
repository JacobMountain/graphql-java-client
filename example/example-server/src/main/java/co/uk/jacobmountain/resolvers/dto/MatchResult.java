package co.uk.jacobmountain.resolvers.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchResult {

    private Score home;

    private Score away;

}
