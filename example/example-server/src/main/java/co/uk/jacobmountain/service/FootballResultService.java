package co.uk.jacobmountain.service;

import co.uk.jacobmountain.resolvers.dto.MatchResult;
import co.uk.jacobmountain.resolvers.dto.Score;
import co.uk.jacobmountain.resolvers.dto.Team;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class FootballResultService implements ResultService {

    private static final Random RANDOM = new Random();

    private static final List<String> TEAMS = Arrays.asList("Arsenal", "Chelsea", "Spurs", "Chelsea", "Tottenham", "Aston Villa", "West Ham United", "Leeds United");

    @Override
    public MatchResult randomResult() {
        return MatchResult.builder()
                .home(randomScore())
                .away(randomScore())
                .build();
    }

    private Score randomScore() {
        return Score.builder()
                .points(RANDOM.nextInt(10) + 1)
                .team(randomTeam())
                .build();
    }

    private Team randomTeam(){
        String team = random(TEAMS);
        return Team.builder()
                .id(UUID.randomUUID().toString())
                .name(team)
                .build();
    }

    private static final <T> T random(List<T> list) {
        return list.get(RANDOM.nextInt(list.size() - 1));
    }

}
