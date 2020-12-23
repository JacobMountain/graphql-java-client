package co.uk.jacobmountain.resolvers;

import co.uk.jacobmountain.resolvers.dto.MatchResult;
import co.uk.jacobmountain.resolvers.dto.Team;
import co.uk.jacobmountain.service.ResultService;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Query implements GraphQLQueryResolver {

    private final ResultService resultService;

    public List<MatchResult> results() {
        log.info("Getting match results");
        return resultService.getResults();
    }

    public MatchResult getResult(String id) {
        log.info("Getting match result {}", id);
        return resultService.getResult(Integer.parseInt(id));
    }

    public Team getTeam(String id) {
        log.info("Getting team {}", id);
        return resultService.getTeam(id);
    }

}
