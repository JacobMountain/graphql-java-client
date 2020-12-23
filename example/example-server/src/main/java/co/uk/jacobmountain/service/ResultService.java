package co.uk.jacobmountain.service;

import co.uk.jacobmountain.resolvers.dto.MatchResult;

import java.util.List;

public interface ResultService {

    MatchResult getResult(int id);

    List<MatchResult> getResults();

}
