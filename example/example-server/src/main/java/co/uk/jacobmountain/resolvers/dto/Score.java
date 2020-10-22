package co.uk.jacobmountain.resolvers.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Score {

    private Team team;

    private int points;

}
