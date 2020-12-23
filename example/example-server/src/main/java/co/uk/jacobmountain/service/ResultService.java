package co.uk.jacobmountain.service;

import co.uk.jacobmountain.resolvers.dto.MatchResult;
import co.uk.jacobmountain.resolvers.dto.Team;

import java.util.List;

public interface ResultService {

    MatchResult getResult(int id);

    List<MatchResult> getResults();

    Team getTeam(String id);

}
